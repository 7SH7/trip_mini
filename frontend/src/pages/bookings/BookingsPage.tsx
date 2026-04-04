import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Typography, Box, Button, Chip, Stack, Skeleton, Card, CardContent, Dialog, DialogTitle, DialogContent, DialogActions, TextField } from '@mui/material'
import { Payment, Cancel, BookOnline, CheckCircle } from '@mui/icons-material'
import { bookingApi } from '../../api/bookings'
import { paymentApi } from '../../api/payments'
import type { BookingResponse } from '../../types'
import { useState } from 'react'

const statusConfig: Record<string, { label: string; color: 'warning' | 'success' | 'error'; icon: React.ReactNode }> = {
  PENDING: { label: '결제 대기', color: 'warning', icon: <Payment sx={{ fontSize: 18 }} /> },
  CONFIRMED: { label: '확정', color: 'success', icon: <CheckCircle sx={{ fontSize: 18 }} /> },
  CANCELLED: { label: '취소됨', color: 'error', icon: <Cancel sx={{ fontSize: 18 }} /> },
}

export default function BookingsPage() {
  const queryClient = useQueryClient()
  const [payDialog, setPayDialog] = useState<BookingResponse | null>(null)
  const [amount, setAmount] = useState('')

  const { data: bookings, isLoading } = useQuery({
    queryKey: ['bookings', 'my'],
    queryFn: () => bookingApi.getMyBookings().then(r => r.data.data || []),
  })

  const cancelMutation = useMutation({
    mutationFn: (id: number) => bookingApi.cancel(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['bookings'] }),
  })

  const payMutation = useMutation({
    mutationFn: async ({ bookingId, amount }: { bookingId: number; amount: number }) => {
      const res = await paymentApi.create({ bookingId, amount })
      if (res.data.data?.id) await paymentApi.complete(res.data.data.id)
      return res
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['bookings'] })
      queryClient.invalidateQueries({ queryKey: ['payments'] })
      setPayDialog(null)
      setAmount('')
    },
  })

  const handlePay = () => {
    if (!payDialog || !amount) return
    payMutation.mutate({ bookingId: payDialog.id, amount: Number(amount) })
  }

  return (
    <Box>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h4">내 예약</Typography>
        <Typography variant="body2" color="text.secondary">예약 현황을 확인하고 관리하세요</Typography>
      </Box>

      {isLoading && <Stack spacing={1.5}>{[1,2,3].map(i => <Skeleton key={i} height={88} variant="rounded" sx={{ borderRadius: 4 }} />)}</Stack>}

      {!bookings?.length && !isLoading && (
        <Box textAlign="center" py={8} sx={{ bgcolor: '#f8fafc', borderRadius: 4, border: '2px dashed #e2e8f0' }}>
          <BookOnline sx={{ fontSize: 56, color: '#cbd5e1', mb: 2 }} />
          <Typography variant="h6" color="text.secondary">예약이 없습니다</Typography>
          <Typography variant="body2" color="text.secondary">여행을 예약해보세요</Typography>
        </Box>
      )}

      <Stack spacing={1.5}>
        {bookings?.map((b: BookingResponse) => {
          const cfg = statusConfig[b.status] || statusConfig.PENDING
          return (
            <Card key={b.id} elevation={0}>
              <CardContent sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', py: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={2} alignItems="center">
                  <Box sx={{
                    width: 44, height: 44, borderRadius: '12px',
                    bgcolor: b.status === 'CONFIRMED' ? '#f0fdf4' : b.status === 'CANCELLED' ? '#fef2f2' : '#fffbeb',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    color: b.status === 'CONFIRMED' ? '#22c55e' : b.status === 'CANCELLED' ? '#ef4444' : '#f59e0b',
                  }}>
                    {cfg.icon}
                  </Box>
                  <Box>
                    <Typography fontWeight={600}>예약 #{b.id}</Typography>
                    <Typography variant="body2" color="text.secondary">
                      여행 #{b.tripId} &middot; {new Date(b.bookedAt).toLocaleDateString()}
                    </Typography>
                  </Box>
                </Stack>
                <Stack direction="row" spacing={1} alignItems="center">
                  <Chip label={cfg.label} color={cfg.color} size="small" variant="outlined" />
                  {b.status === 'PENDING' && (
                    <>
                      <Button size="small" variant="contained" disableElevation startIcon={<Payment />}
                        onClick={() => setPayDialog(b)}
                        sx={{ background: 'linear-gradient(135deg, #3b82f6, #2563eb)' }}>결제</Button>
                      <Button size="small" variant="outlined" color="error" startIcon={<Cancel />}
                        onClick={() => cancelMutation.mutate(b.id)}>취소</Button>
                    </>
                  )}
                </Stack>
              </CardContent>
            </Card>
          )
        })}
      </Stack>

      <Dialog open={!!payDialog} onClose={() => setPayDialog(null)} maxWidth="xs" fullWidth
        PaperProps={{ sx: { borderRadius: 3 } }}>
        <DialogTitle sx={{ fontWeight: 700 }}>결제하기</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            예약 #{payDialog?.id} (여행 #{payDialog?.tripId})
          </Typography>
          <TextField fullWidth label="결제 금액 (원)" type="number" value={amount}
            onChange={e => setAmount(e.target.value)} autoFocus />
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setPayDialog(null)} sx={{ color: 'text.secondary' }}>취소</Button>
          <Button variant="contained" onClick={handlePay} disabled={!amount || payMutation.isPending} disableElevation>
            {payMutation.isPending ? '처리 중...' : '결제 완료'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
