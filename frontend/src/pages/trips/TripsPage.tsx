import { Link } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { Typography, Button, Box, Chip, Stack, Skeleton, Card, CardContent, CardActionArea, Grid } from '@mui/material'
import { Add, Flight, CalendarMonth, ArrowForward, GroupAdd } from '@mui/icons-material'
import { tripApi } from '../../api/trips'
import type { TripResponse } from '../../types'
import { useState } from 'react'
import { Dialog, DialogTitle, DialogContent, DialogActions, TextField, Alert } from '@mui/material'

const statusConfig: Record<string, { label: string; color: 'info' | 'warning' | 'success' | 'error' }> = {
  PLANNED: { label: '예정', color: 'info' },
  IN_PROGRESS: { label: '진행 중', color: 'warning' },
  COMPLETED: { label: '완료', color: 'success' },
  CANCELLED: { label: '취소됨', color: 'error' },
}

export default function TripsPage() {
  const [joinOpen, setJoinOpen] = useState(false)
  const [joinCode, setJoinCode] = useState('')
  const [joinError, setJoinError] = useState('')
  const [joinSuccess, setJoinSuccess] = useState(false)

  const joinMutation = useMutation({
    mutationFn: () => tripApi.joinByCode(joinCode),
    onSuccess: () => { setJoinSuccess(true); setJoinCode(''); setTimeout(() => { setJoinOpen(false); setJoinSuccess(false) }, 2000) },
    onError: () => setJoinError('유효하지 않거나 만료된 초대 코드입니다.'),
  })

  const { data: trips, isLoading } = useQuery({
    queryKey: ['trips', 'my'],
    queryFn: () => tripApi.getMyTrips().then(r => r.data.data || []),
  })

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Box>
          <Typography variant="h4">내 여행</Typography>
          <Typography variant="body2" color="text.secondary">여행 일정을 관리하고 예약하세요</Typography>
        </Box>
        <Stack direction="row" spacing={1}>
          <Button variant="outlined" startIcon={<GroupAdd />} onClick={() => setJoinOpen(true)}
            sx={{ borderColor: '#e2e8f0', color: 'text.secondary' }}>
            코드로 참여
          </Button>
          <Button component={Link} to="/trips/new" variant="contained" startIcon={<Add />} disableElevation
            sx={{ background: 'linear-gradient(135deg, #3b82f6, #2563eb)' }}>
            새 여행
          </Button>
        </Stack>
      </Stack>

      {isLoading && (
        <Grid container spacing={2}>
          {[1,2,3].map(i => <Grid size={{ xs: 12, sm: 6, md: 4 }} key={i}><Skeleton height={180} variant="rounded" sx={{ borderRadius: 4 }} /></Grid>)}
        </Grid>
      )}

      {!trips?.length && !isLoading && (
        <Box textAlign="center" py={8} sx={{ bgcolor: '#f8fafc', borderRadius: 4, border: '2px dashed #e2e8f0' }}>
          <Flight sx={{ fontSize: 56, color: '#cbd5e1', mb: 2 }} />
          <Typography variant="h6" color="text.secondary" gutterBottom>아직 여행이 없습니다</Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>첫 여행을 계획해보세요!</Typography>
          <Button component={Link} to="/trips/new" variant="contained" startIcon={<Add />} disableElevation>여행 만들기</Button>
        </Box>
      )}

      <Grid container spacing={2}>
        {trips?.map((trip: TripResponse) => {
          const cfg = statusConfig[trip.status] || statusConfig.PLANNED
          return (
            <Grid size={{ xs: 12, sm: 6, md: 4 }} key={trip.id}>
              <Card elevation={0}>
                <CardActionArea component={Link} to={`/trips/${trip.id}`}>
                  <Box sx={{
                    height: 8, borderRadius: '16px 16px 0 0',
                    background: trip.status === 'IN_PROGRESS' ? 'linear-gradient(90deg, #f59e0b, #f97316)' :
                      trip.status === 'COMPLETED' ? 'linear-gradient(90deg, #22c55e, #06b6d4)' :
                      trip.status === 'CANCELLED' ? '#ef4444' : 'linear-gradient(90deg, #3b82f6, #06b6d4)',
                  }} />
                  <CardContent sx={{ p: 2.5 }}>
                    <Stack direction="row" justifyContent="space-between" alignItems="flex-start" sx={{ mb: 1.5 }}>
                      <Typography variant="h6" sx={{ fontSize: '1.05rem' }}>{trip.title}</Typography>
                      <Chip label={cfg.label} color={cfg.color} size="small" variant="outlined" />
                    </Stack>
                    {trip.description && (
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                        {trip.description}
                      </Typography>
                    )}
                    <Stack direction="row" justifyContent="space-between" alignItems="center">
                      <Stack direction="row" spacing={0.5} alignItems="center" sx={{ color: 'text.secondary' }}>
                        <CalendarMonth sx={{ fontSize: 16 }} />
                        <Typography variant="caption">{trip.startDate} ~ {trip.endDate}</Typography>
                      </Stack>
                      <ArrowForward sx={{ fontSize: 16, color: 'text.secondary' }} />
                    </Stack>
                  </CardContent>
                </CardActionArea>
              </Card>
            </Grid>
          )
        })}
      </Grid>

      <Dialog open={joinOpen} onClose={() => { setJoinOpen(false); setJoinError('') }} maxWidth="xs" fullWidth
        PaperProps={{ sx: { borderRadius: 3 } }}>
        <DialogTitle fontWeight={700}>초대 코드로 참여</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            친구에게 받은 8자리 초대 코드를 입력하세요
          </Typography>
          {joinError && <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>{joinError}</Alert>}
          {joinSuccess && <Alert severity="success" sx={{ mb: 2, borderRadius: 2 }}>참여 요청을 보냈습니다! 방장의 승인을 기다려주세요.</Alert>}
          {!joinSuccess && <TextField fullWidth label="초대 코드" value={joinCode} onChange={e => setJoinCode(e.target.value.toUpperCase())}
            placeholder="예: A1B2C3D4" inputProps={{ maxLength: 8, style: { letterSpacing: '0.15em', fontWeight: 700 } }} />}
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setJoinOpen(false)} sx={{ color: 'text.secondary' }}>취소</Button>
          <Button variant="contained" onClick={() => joinMutation.mutate()} disabled={joinCode.length < 8 || joinMutation.isPending}
            disableElevation>참여하기</Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
