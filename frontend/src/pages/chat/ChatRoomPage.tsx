import { useState, useEffect, useRef } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Paper, Typography, Box, TextField, IconButton, Stack, Avatar, Chip } from '@mui/material'
import { Send, People } from '@mui/icons-material'
import styled from 'styled-components'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { chatApi } from '../../api/chat'
import type { ChatMessageResponse } from '../../types'
import { useAppSelector } from '../../store/hooks'

const MessagesContainer = styled.div`
  height: 500px;
  overflow-y: auto;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
`

const MessageBubble = styled.div<{ $isMine: boolean }>`
  max-width: 70%;
  padding: 0.6rem 1rem;
  border-radius: 12px;
  align-self: ${p => p.$isMine ? 'flex-end' : 'flex-start'};
  background: ${p => p.$isMine ? '#2563eb' : '#f3f4f6'};
  color: ${p => p.$isMine ? 'white' : '#333'};
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

  // Load message history
  useEffect(() => {
    if (roomId) {
      chatApi.getMessages(Number(roomId)).then(r => {
        setMessages(r.data.data?.content?.reverse() || [])
      })
    }
  }, [roomId])

  // Connect WebSocket
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

    return () => {
      chatApi.leaveRoom(Number(roomId))
      client.deactivate()
    }
  }, [roomId])

  // Auto scroll
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
    <Paper sx={{ p: 0, overflow: 'hidden' }} elevation={0}>
      <Box sx={{ p: 2, borderBottom: '1px solid #e5e7eb', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h6" fontWeight={600}>{room?.name || '채팅방'}</Typography>
          <Typography variant="caption" color="text.secondary">
            {room?.centerLatitude?.toFixed(2)}, {room?.centerLongitude?.toFixed(2)}
          </Typography>
        </Box>
        <Chip icon={<People />} label={`${onlineUsers?.length || 0}명 접속`} size="small" />
      </Box>

      <MessagesContainer>
        {messages.map(msg => (
          <Box key={msg.id} sx={{ display: 'flex', flexDirection: 'column', alignItems: msg.userId === currentUserId ? 'flex-end' : 'flex-start' }}>
            {msg.userId !== currentUserId && (
              <Stack direction="row" spacing={0.5} alignItems="center" sx={{ mb: 0.3 }}>
                <Avatar sx={{ width: 20, height: 20, fontSize: 10 }}>{msg.userId}</Avatar>
                <Typography variant="caption" color="text.secondary">User #{msg.userId}</Typography>
              </Stack>
            )}
            <MessageBubble $isMine={msg.userId === currentUserId}>
              <Typography variant="body2">{msg.content}</Typography>
            </MessageBubble>
            <Typography variant="caption" color="text.secondary" sx={{ mt: 0.2 }}>
              {new Date(msg.sentAt).toLocaleTimeString()}
            </Typography>
          </Box>
        ))}
        <div ref={messagesEndRef} />
      </MessagesContainer>

      <Box sx={{ p: 2, borderTop: '1px solid #e5e7eb', display: 'flex', gap: 1 }}>
        <TextField
          fullWidth size="small" placeholder="메시지 입력..."
          value={message} onChange={e => setMessage(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && handleSend()}
        />
        <IconButton color="primary" onClick={handleSend} disabled={!message.trim()}>
          <Send />
        </IconButton>
      </Box>
    </Paper>
  )
}
