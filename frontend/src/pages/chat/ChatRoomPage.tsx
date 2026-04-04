import { useState, useEffect, useRef } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Typography, Box, TextField, IconButton, Stack, Avatar, Chip, Paper } from '@mui/material'
import { Send, People } from '@mui/icons-material'
import styled from '@emotion/styled'
import { keyframes } from '@emotion/react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { chatApi } from '../../api/chat'
import type { ChatMessageResponse } from '../../types'
import { useAppSelector } from '../../store/hooks'
import { motion } from 'framer-motion'

const fadeIn = keyframes`
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
`

const MessagesContainer = styled.div`
  height: 520px;
  overflow-y: auto;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
`

const Bubble = styled.div<{ $mine: boolean }>`
  max-width: 70%;
  padding: 0.75rem 1rem;
  border-radius: ${p => p.$mine ? '20px 20px 4px 20px' : '20px 20px 20px 4px'};
  align-self: ${p => p.$mine ? 'flex-end' : 'flex-start'};
  background: ${p => p.$mine ? 'linear-gradient(135deg, #3b82f6, #8b5cf6)' : 'white'};
  color: ${p => p.$mine ? 'white' : '#1e293b'};
  box-shadow: ${p => p.$mine ? '0 2px 12px rgba(59,130,246,0.2)' : '0 1px 4px rgba(0,0,0,0.06)'};
  line-height: 1.55;
  font-size: 0.9rem;
  animation: ${fadeIn} 0.25s ease-out;
`

export default function ChatRoomPage() {
  const { roomId } = useParams<{ roomId: string }>()
  const [message, setMessage] = useState('')
  const [messages, setMessages] = useState<ChatMessageResponse[]>([])
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const stompClientRef = useRef<Client | null>(null)
  const { user } = useAppSelector(state => state.auth)
  const currentUserId = user?.id ?? 0

  const { data: room } = useQuery({
    queryKey: ['chatRoom', roomId],
    queryFn: () => chatApi.getRoom(Number(roomId)).then(r => r.data.data),
    enabled: !!roomId,
  })

  const { data: onlineUsers } = useQuery({
    queryKey: ['chatRoom', roomId, 'online'],
    queryFn: () => chatApi.getOnlineUsers(Number(roomId)).then(r => r.data.data),
    enabled: !!roomId,
    refetchInterval: 5000,
  })

  useEffect(() => {
    if (roomId) {
      chatApi.getMessages(Number(roomId)).then(r => {
        setMessages(r.data.data?.content?.reverse() || [])
      })
    }
  }, [roomId])

  useEffect(() => {
    if (!roomId) return
    chatApi.joinRoom(Number(roomId))
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8089/ws/chat'),
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/chat/${roomId}`, (msg) => {
          const chatMsg: ChatMessageResponse = JSON.parse(msg.body)
          setMessages(prev => [...prev, chatMsg])
        })
      },
    })
    client.activate()
    stompClientRef.current = client
    return () => { chatApi.leaveRoom(Number(roomId)); client.deactivate() }
  }, [roomId])

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const handleSend = () => {
    if (!message.trim() || !stompClientRef.current?.connected) return
    stompClientRef.current.publish({
      destination: `/app/chat/${roomId}`,
      headers: { 'X-User-Id': String(currentUserId) },
      body: JSON.stringify({ content: message, type: 'TEXT' }),
    })
    setMessage('')
  }

  return (
    <motion.div initial={{ opacity: 0, scale: 0.98 }} animate={{ opacity: 1, scale: 1 }} transition={{ duration: 0.3 }}>
      <Paper elevation={0} sx={{ overflow: 'hidden', border: '1px solid #e2e8f0', borderRadius: 4 }}>
        {/* Header */}
        <Box sx={{
          p: 2.5,
          background: 'linear-gradient(135deg, #22c55e 0%, #06b6d4 60%, #3b82f6 100%)',
          color: 'white',
          display: 'flex', justifyContent: 'space-between', alignItems: 'center',
        }}>
          <Box>
            <Typography variant="h6" fontWeight={700}>{room?.name || '채팅방'}</Typography>
            <Typography variant="caption" sx={{ opacity: 0.75 }}>
              {room?.centerLatitude?.toFixed(2)}, {room?.centerLongitude?.toFixed(2)}
            </Typography>
          </Box>
          <Chip icon={<People sx={{ color: 'white !important', fontSize: 16 }} />}
            label={`${onlineUsers?.length || 0}명 접속`} size="small"
            sx={{ bgcolor: 'rgba(255,255,255,0.18)', color: 'white', fontWeight: 600, backdropFilter: 'blur(8px)' }} />
        </Box>

        {/* Messages */}
        <MessagesContainer>
          {messages.map(msg => (
            <Box key={msg.id} sx={{
              display: 'flex', flexDirection: 'column',
              alignItems: msg.userId === currentUserId ? 'flex-end' : 'flex-start',
            }}>
              {msg.userId !== currentUserId && (
                <Stack direction="row" spacing={0.5} alignItems="center" sx={{ mb: 0.3 }}>
                  <Avatar sx={{
                    width: 22, height: 22, fontSize: 10,
                    background: 'linear-gradient(135deg, #e2e8f0, #cbd5e1)', color: '#475569',
                  }}>{msg.userId}</Avatar>
                  <Typography variant="caption" sx={{ color: '#94a3b8', fontSize: '0.7rem' }}>User #{msg.userId}</Typography>
                </Stack>
              )}
              <Bubble $mine={msg.userId === currentUserId}>
                {msg.content}
              </Bubble>
              <Typography variant="caption" sx={{ color: '#94a3b8', mt: 0.2, px: 0.5, fontSize: '0.65rem' }}>
                {new Date(msg.sentAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </Typography>
            </Box>
          ))}
          <div ref={messagesEndRef} />
        </MessagesContainer>

        {/* Input */}
        <Box sx={{
          p: 2, bgcolor: 'white', borderTop: '1px solid #f1f5f9',
          display: 'flex', gap: 1, alignItems: 'center',
        }}>
          <TextField fullWidth size="small" placeholder="메시지 입력..."
            value={message} onChange={e => setMessage(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && !e.shiftKey && (e.preventDefault(), handleSend())}
            sx={{
              '& .MuiOutlinedInput-root': {
                borderRadius: '14px', bgcolor: '#f8fafc',
                '&:hover fieldset': { borderColor: '#cbd5e1' },
              },
            }} />
          <IconButton onClick={handleSend} disabled={!message.trim()}
            sx={{
              width: 44, height: 44, borderRadius: '14px',
              background: message.trim() ? 'linear-gradient(135deg, #3b82f6, #8b5cf6)' : '#f1f5f9',
              color: message.trim() ? 'white' : '#94a3b8',
              transition: 'all 0.2s',
              '&:hover': { background: 'linear-gradient(135deg, #2563eb, #7c3aed)', color: 'white' },
            }}>
            <Send fontSize="small" />
          </IconButton>
        </Box>
      </Paper>
    </motion.div>
  )
}
