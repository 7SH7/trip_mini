import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Paper, Typography, Box, Button, Chip, Stack, Skeleton, Card, CardContent } from '@mui/material'
import { Payment, Cancel, BookOnline } from '@mui/icons-material'
import { bookingApi } from '../../api/bookings'
import { paymentApi } from '../../api/payments'
import type { BookingResponse } from '../../types'

const statusColor: Record<string, 'warning' | 'success' | 'error'> = {
  PENDING: 'warning', CONFIRMED: 'success', CANCELLED: 'error',
}

export default function BookingsPage() {
  const queryClient = useQueryClient()

  const { data: bookings, isLoading } = useQuery({
    queryKey: ['bookings', 'my'],
    queryFn: () => bookingApi.getMyBookings().then(r => r.data.data || []),
  })

  const cancelMutation = useMutation({
    mutationFn: (id: number) => bookingApi.cancel(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['bookings'] }),
  })

  const payMutation = useMutation({
    mutationFn: async (booking: BookingResponse) => {
      const amount = prompt('결제 금액을 입력하세요')
      if (!amount) throw new Error('cancelled')
      const res = await paymentApi.create({ bookingId: booking.id, amount: Number(amount) })
      if (res.data.data?.id) await paymentApi.complete(res.data.data.id)
      return res
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['bookings'] }),
  })

  return (
    <Paper sx={{ p: 3 }} elevation={0}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Typography variant="h5" fontWeight={600}>내 예약</Typography>
      </Stack>

      {isLoading && [1,2,3].map(i => <Skeleton key={i} height={80} variant="rounded" sx={{ mb: 1 }} />)}

      {!bookings?.length && !isLoading && (
        <Box textAlign="center" py={5} color="#999">
          <BookOnline sx={{ fontSize: 48, mb: 1 }} />
          <Typography>예약이 없습니다.</Typography>
        </Box>
      )}

      <Stack spacing={1.5}>
        {bookings?.map((b: BookingResponse) => (
          <Card key={b.id} elevation={0} variant="outlined">
            <CardContent sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Box>
                <Typography fontWeight={600}>예약 #{b.id}</Typography>
                <Typography variant="body2" color="text.secondary">
                  여행 #{b.tripId} | {new Date(b.bookedAt).toLocaleDateString()}
                </Typography>
              </Box>
              <Stack direction="row" spacing={1} alignItems="center">
                <Chip label={b.status} color={statusColor[b.status]} size="small" />
                {b.status === 'PENDING' && (
                  <>
                    <Button size="small" variant="contained" startIcon={<Payment />}
                      onClick={() => payMutation.mutate(b)}>결제</Button>
                    <Button size="small" variant="outlined" color="error" startIcon={<Cancel />}
                      onClick={() => cancelMutation.mutate(b.id)}>취소</Button>
                  </>
                )}
              </Stack>
            </CardContent>
          </Card>
        ))}
      </Stack>
    </Paper>
  )
}
