import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { Typography, Button, Box, Alert, CircularProgress, Paper, Stack } from '@mui/material'
import { MyLocation, Chat, NearMe } from '@mui/icons-material'
import { chatApi } from '../../api/chat'

export default function ChatPage() {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()

  const findRoomMutation = useMutation({
    mutationFn: (coords: { latitude: number; longitude: number }) => chatApi.findNearbyRoom(coords),
    onSuccess: (res) => {
      const room = res.data.data
      if (room) navigate(`/chat/${room.id}`)
    },
    onError: () => setError('채팅방을 찾을 수 없습니다.'),
  })

  const handleFindRoom = () => {
    setLoading(true)
    setError('')
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setLoading(false)
        findRoomMutation.mutate({ latitude: pos.coords.latitude, longitude: pos.coords.longitude })
      },
      () => {
        setLoading(false)
        setError('위치 정보를 가져올 수 없습니다. 위치 권한을 허용해주세요.')
      }
    )
  }

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
      <Paper elevation={0} sx={{ p: 5, maxWidth: 480, width: '100%', textAlign: 'center', border: '1px solid #e2e8f0' }}>
        <Box sx={{
          width: 72, height: 72, borderRadius: '20px', mx: 'auto', mb: 3,
          background: 'linear-gradient(135deg, #22c55e 0%, #06b6d4 100%)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          boxShadow: '0 8px 32px rgba(34,197,94,0.3)',
        }}>
          <Chat sx={{ color: 'white', fontSize: 36 }} />
        </Box>

        <Typography variant="h5" sx={{ mb: 1 }}>GPS 채팅</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 4 }}>
          현재 위치를 기반으로 반경 5km 내 여행자들과<br />실시간으로 대���할 수 있습니다
        </Typography>

        {error && <Alert severity="error" sx={{ mb: 3, borderRadius: 2, textAlign: 'left' }}>{error}</Alert>}

        <Button variant="contained" size="large" fullWidth disableElevation
          onClick={handleFindRoom} disabled={loading || findRoomMutation.isPending}
          startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <MyLocation />}
          sx={{ py: 1.5, background: 'linear-gradient(135deg, #22c55e, #06b6d4)' }}>
          {loading ? '위치 확인 중...' : findRoomMutation.isPending ? '채팅방 찾는 중...' : '주변 채팅방 찾기'}
        </Button>

        <Stack direction="row" spacing={0.5} justifyContent="center" alignItems="center" sx={{ mt: 3, color: 'text.secondary' }}>
          <NearMe sx={{ fontSize: 14 }} />
          <Typography variant="caption">위치 권한이 필요합니다</Typography>
        </Stack>
      </Paper>
    </Box>
  )
}
