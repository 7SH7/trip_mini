import { useState, useEffect, useRef } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Typography, Box, TextField, IconButton, Stack, Avatar, Chip, Paper } from '@mui/material'
import { Send, People } from '@mui/icons-material'
import styled from 'styled-components'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { chatApi } from '../../api/chat'
import type { ChatMessageResponse } from '../../types'
import { useAppSelector } from '../../store/hooks'

const MessagesContainer = styled.div`
  height: 520px;
  overflow-y: auto;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  background: #f8fafc;
`

const MessageBubble = styled.div<{ $isMine: boolean }>`
  max-width: 70%;
  padding: 0.7rem 1rem;
  border-radius: ${p => p.$isMine ? '16px 16px 4px 16px' : '16px 16px 16px 4px'};
  align-self: ${p => p.$isMine ? 'flex-end' : 'flex-start'};
  background: ${p => p.$isMine ? 'linear-gradient(135deg, #3b82f6, #2563eb)' : 'white'};
  color: ${p => p.$isMine ? 'white' : '#1e293b'};
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  line-height: 1.5;
  font-size: 0.9rem;
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
    <Paper elevation={0} sx={{ overflow: 'hidden', border: '1px solid #e2e8f0', borderRadius: 4 }}>
      {/* Header */}
      <Box sx={{
        p: 2.5, background: 'linear-gradient(135deg, #22c55e 0%, #06b6d4 100%)', color: 'white',
        display: 'flex', justifyContent: 'space-between', alignItems: 'center',
      }}>
        <Box>
          <Typography variant="h6" fontWeight={700}>{room?.name || '채팅방'}</Typography>
          <Typography variant="caption" sx={{ opacity: 0.8 }}>
            {room?.centerLatitude?.toFixed(2)}, {room?.centerLongitude?.toFixed(2)}
          </Typography>
        </Box>
        <Chip icon={<People sx={{ color: 'white !important' }} />}
          label={`${onlineUsers?.length || 0}명 접속`} size="small"
          sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: 'white', fontWeight: 600, '& .MuiChip-icon': { color: 'white' } }} />
      </Box>

      {/* Messages */}
      <MessagesContainer>
        {messages.map(msg => (
          <Box key={msg.id} sx={{ display: 'flex', flexDirection: 'column', alignItems: msg.userId === currentUserId ? 'flex-end' : 'flex-start' }}>
            {msg.userId !== currentUserId && (
              <Stack direction="row" spacing={0.5} alignItems="center" sx={{ mb: 0.3 }}>
                <Avatar sx={{ width: 22, height: 22, fontSize: 10, bgcolor: '#e2e8f0', color: '#64748b' }}>{msg.userId}</Avatar>
                <Typography variant="caption" color="text.secondary">User #{msg.userId}</Typography>
              </Stack>
            )}
            <MessageBubble $isMine={msg.userId === currentUserId}>
              {msg.content}
            </MessageBubble>
            <Typography variant="caption" color="text.secondary" sx={{ mt: 0.2, px: 0.5 }}>
              {new Date(msg.sentAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </Typography>
          </Box>
        ))}
        <div ref={messagesEndRef} />
      </MessagesContainer>

      {/* Input */}
      <Box sx={{ p: 2, bgcolor: 'white', borderTop: '1px solid #e2e8f0', display: 'flex', gap: 1 }}>
        <TextField fullWidth size="small" placeholder="메시지 입력..."
          value={message} onChange={e => setMessage(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && !e.shiftKey && (e.preventDefault(), handleSend())}
          sx={{ '& .MuiOutlinedInput-root': { borderRadius: '12px' } }} />
        <IconButton color="primary" onClick={handleSend} disabled={!message.trim()}
          sx={{ bgcolor: message.trim() ? 'primary.main' : '#f1f5f9', color: message.trim() ? 'white' : '#94a3b8',
            '&:hover': { bgcolor: 'primary.dark', color: 'white' }, borderRadius: '12px', width: 44, height: 44 }}>
          <Send fontSize="small" />
        </IconButton>
      </Box>
    </Paper>
  )
}
