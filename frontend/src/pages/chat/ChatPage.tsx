import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { Paper, Typography, Box, Button, Alert, CircularProgress } from '@mui/material'
import { MyLocation, Chat as ChatIcon } from '@mui/icons-material'
import { chatApi } from '../../api/chat'

export default function ChatPage() {
  const navigate = useNavigate()
  const [error, setError] = useState('')

  const findRoomMutation = useMutation({
    mutationFn: () => new Promise<{ latitude: number; longitude: number }>((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(
        pos => resolve({ latitude: pos.coords.latitude, longitude: pos.coords.longitude }),
        () => reject(new Error('위치 정보를 가져올 수 없습니다.'))
      )
    }).then(coords => chatApi.findNearbyRoom(coords)),
    onSuccess: (res) => {
      const room = res.data.data!
      navigate(`/chat/${room.id}`)
    },
    onError: (err: Error) => setError(err.message),
  })

  return (
    <Paper sx={{ p: 3 }} elevation={0}>
      <Typography variant="h5" fontWeight={600} gutterBottom>GPS 채팅</Typography>
      <Box textAlign="center" py={5}>
        <ChatIcon sx={{ fontSize: 64, color: 'primary.main', mb: 2 }} />
        <Typography variant="h6" gutterBottom>내 위치 근처의 채팅방에 참여하세요</Typography>
        <Typography color="text.secondary" sx={{ mb: 3 }}>
          GPS 기반으로 5km 이내 채팅방을 찾거나 새로 만듭니다.
        </Typography>
        <Button
          variant="contained" size="large" startIcon={<MyLocation />}
          onClick={() => findRoomMutation.mutate()}
          disabled={findRoomMutation.isPending}
        >
          {findRoomMutation.isPending ? <CircularProgress size={24} /> : '근처 채팅방 찾기'}
        </Button>
        {error && <Alert severity="error" sx={{ mt: 2, maxWidth: 400, mx: 'auto' }}>{error}</Alert>}
      </Box>
    </Paper>
  )
}
