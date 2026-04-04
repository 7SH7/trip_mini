import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { Typography, Button, Chip, Box, Paper, Skeleton, Stack, Divider } from '@mui/material'
import { ArrowBack, CalendarMonth, BookOnline, Info } from '@mui/icons-material'
import { tripApi } from '../../api/trips'
import { bookingApi } from '../../api/bookings'

const statusConfig: Record<string, { label: string; color: 'info' | 'warning' | 'success' | 'error' }> = {
  PLANNED: { label: '예정', color: 'info' },
  IN_PROGRESS: { label: '진행 중', color: 'warning' },
  COMPLETED: { label: '완료', color: 'success' },
  CANCELLED: { label: '취소됨', color: 'error' },
}

export default function TripDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const { data: trip, isLoading } = useQuery({
    queryKey: ['trip', id],
    queryFn: () => tripApi.getById(Number(id)).then(r => r.data.data),
    enabled: !!id,
  })

  const bookMutation = useMutation({
    mutationFn: () => bookingApi.create({ tripId: Number(id) }),
    onSuccess: () => navigate('/bookings'),
  })

  if (isLoading) return <Stack spacing={2}><Skeleton height={200} variant="rounded" sx={{ borderRadius: 4 }} /><Skeleton height={100} variant="rounded" sx={{ borderRadius: 4 }} /></Stack>
  if (!trip) return <Typography color="text.secondary">여행을 찾을 수 없습니다.</Typography>

  const cfg = statusConfig[trip.status] || statusConfig.PLANNED

  return (
    <Box sx={{ maxWidth: 700, mx: 'auto' }}>
      <Button startIcon={<ArrowBack />} onClick={() => navigate('/trips')} sx={{ mb: 2, color: 'text.secondary' }}>
        목록으로
      </Button>
      <Paper elevation={0} sx={{ border: '1px solid #e2e8f0', overflow: 'hidden' }}>
        <Box sx={{
          height: 12,
          background: trip.status === 'IN_PROGRESS' ? 'linear-gradient(90deg, #f59e0b, #f97316)' :
            trip.status === 'COMPLETED' ? 'linear-gradient(90deg, #22c55e, #06b6d4)' :
            trip.status === 'CANCELLED' ? '#ef4444' : 'linear-gradient(90deg, #3b82f6, #06b6d4)',
        }} />
        <Box sx={{ p: 4 }}>
          <Stack direction="row" justifyContent="space-between" alignItems="flex-start" sx={{ mb: 3 }}>
            <Typography variant="h4">{trip.title}</Typography>
            <Chip label={cfg.label} color={cfg.color} sx={{ fontWeight: 700 }} />
          </Stack>

          {trip.description && (
            <Typography variant="body1" color="text.secondary" sx={{ mb: 3, lineHeight: 1.7 }}>
              {trip.description}
            </Typography>
          )}

          <Divider sx={{ my: 3 }} />

          <Stack spacing={2}>
            <Stack direction="row" spacing={1.5} alignItems="center">
              <Box sx={{ width: 40, height: 40, borderRadius: '10px', bgcolor: '#eff6ff', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <CalendarMonth sx={{ color: '#3b82f6', fontSize: 20 }} />
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">여행 기간</Typography>
                <Typography variant="body2" fontWeight={600}>{trip.startDate} ~ {trip.endDate}</Typography>
              </Box>
            </Stack>
            <Stack direction="row" spacing={1.5} alignItems="center">
              <Box sx={{ width: 40, height: 40, borderRadius: '10px', bgcolor: '#f0fdf4', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <Info sx={{ color: '#22c55e', fontSize: 20 }} />
              </Box>
              <Box>
                <Typography variant="caption" color="text.secondary">생성일</Typography>
                <Typography variant="body2" fontWeight={600}>{new Date(trip.createdAt).toLocaleDateString()}</Typography>
              </Box>
            </Stack>
          </Stack>

          {trip.status === 'PLANNED' && (
            <>
              <Divider sx={{ my: 3 }} />
              <Button variant="contained" size="large" fullWidth startIcon={<BookOnline />}
                onClick={() => bookMutation.mutate()} disabled={bookMutation.isPending} disableElevation
                sx={{ py: 1.5, background: 'linear-gradient(135deg, #3b82f6, #2563eb)' }}>
                {bookMutation.isPending ? '예약 처리 중...' : '이 여행 예약하기'}
              </Button>
            </>
          )}
        </Box>
      </Paper>
    </Box>
  )
}
