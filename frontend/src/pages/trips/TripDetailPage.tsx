import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { Paper, Typography, Box, Button, Chip, Stack, Skeleton, Divider } from '@mui/material'
import { BookOnline, ArrowBack } from '@mui/icons-material'
import { tripApi } from '../../api/trips'
import { bookingApi } from '../../api/bookings'

const statusColor: Record<string, 'info' | 'warning' | 'success' | 'error'> = {
  PLANNED: 'info', IN_PROGRESS: 'warning', COMPLETED: 'success', CANCELLED: 'error',
}

export default function TripDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const { data: trip, isLoading } = useQuery({
    queryKey: ['trip', id],
    queryFn: () => tripApi.getById(Number(id)).then(r => r.data.data),
    enabled: !!id,
  })

  const bookingMutation = useMutation({
    mutationFn: () => bookingApi.create({ tripId: Number(id) }),
    onSuccess: () => navigate('/bookings'),
  })

  if (isLoading) return <Paper sx={{ p: 3 }} elevation={0}><Skeleton height={300} variant="rounded" /></Paper>
  if (!trip) return <Paper sx={{ p: 3 }} elevation={0}><Typography>여행을 찾을 수 없습니다.</Typography></Paper>

  return (
    <Paper sx={{ p: 3, maxWidth: 600, mx: 'auto' }} elevation={0}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
        <Typography variant="h5" fontWeight={600}>{trip.title}</Typography>
        <Chip label={trip.status} color={statusColor[trip.status]} />
      </Stack>

      {trip.description && (
        <Typography color="text.secondary" sx={{ mb: 2 }}>{trip.description}</Typography>
      )}

      <Divider sx={{ my: 2 }} />

      <Stack spacing={1.5}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
          <Typography color="text.secondary">시작일</Typography>
          <Typography fontWeight={500}>{trip.startDate}</Typography>
        </Box>
        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
          <Typography color="text.secondary">종료일</Typography>
          <Typography fontWeight={500}>{trip.endDate}</Typography>
        </Box>
        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
          <Typography color="text.secondary">생성일</Typography>
          <Typography fontWeight={500}>{new Date(trip.createdAt).toLocaleDateString()}</Typography>
        </Box>
      </Stack>

      <Stack direction="row" spacing={1} sx={{ mt: 3 }}>
        {trip.status === 'PLANNED' && (
          <Button variant="contained" startIcon={<BookOnline />}
            onClick={() => bookingMutation.mutate()}
            disabled={bookingMutation.isPending}>
            {bookingMutation.isPending ? '예약 중...' : '예약하기'}
          </Button>
        )}
        <Button variant="outlined" startIcon={<ArrowBack />} onClick={() => navigate('/trips')}>
          목록으로
        </Button>
      </Stack>
    </Paper>
  )
}
