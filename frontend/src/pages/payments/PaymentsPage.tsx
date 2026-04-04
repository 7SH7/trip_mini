import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Typography, Box, Button, Chip, Stack, Skeleton, Card, CardContent } from '@mui/material'
import { Payment, Undo, CheckCircle, Error, HourglassEmpty, Replay } from '@mui/icons-material'
import { paymentApi } from '../../api/payments'
import type { PaymentResponse } from '../../types'

const statusConfig: Record<string, { label: string; color: 'warning' | 'success' | 'error' | 'info'; icon: React.ReactNode }> = {
  PENDING: { label: '대기', color: 'warning', icon: <HourglassEmpty sx={{ fontSize: 20 }} /> },
  COMPLETED: { label: '완료', color: 'success', icon: <CheckCircle sx={{ fontSize: 20 }} /> },
  FAILED: { label: '실패', color: 'error', icon: <Error sx={{ fontSize: 20 }} /> },
  REFUNDED: { label: '환불', color: 'info', icon: <Replay sx={{ fontSize: 20 }} /> },
}

export default function PaymentsPage() {
  const queryClient = useQueryClient()

  const { data: payments, isLoading } = useQuery({
    queryKey: ['payments', 'my'],
    queryFn: () => paymentApi.getMyPayments().then(r => r.data.data || []),
  })

  const refundMutation = useMutation({
    mutationFn: (id: number) => paymentApi.refund(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['payments'] }),
  })

  return (
    <Box>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h4">결제 내역</Typography>
        <Typography variant="body2" color="text.secondary">결제 현황을 확인하고 환불을 요청하세요</Typography>
      </Box>

      {isLoading && <Stack spacing={1.5}>{[1,2,3].map(i => <Skeleton key={i} height={88} variant="rounded" sx={{ borderRadius: 4 }} />)}</Stack>}

      {!payments?.length && !isLoading && (
        <Box textAlign="center" py={8} sx={{ bgcolor: '#f8fafc', borderRadius: 4, border: '2px dashed #e2e8f0' }}>
          <Payment sx={{ fontSize: 56, color: '#cbd5e1', mb: 2 }} />
          <Typography variant="h6" color="text.secondary">결제 내역이 없습니다</Typography>
        </Box>
      )}

      <Stack spacing={1.5}>
        {payments?.map((p: PaymentResponse) => {
          const cfg = statusConfig[p.status] || statusConfig.PENDING
          return (
            <Card key={p.id} elevation={0}>
              <CardContent sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', py: 2, '&:last-child': { pb: 2 } }}>
                <Stack direction="row" spacing={2} alignItems="center">
                  <Box sx={{
                    width: 44, height: 44, borderRadius: '12px',
                    bgcolor: p.status === 'COMPLETED' ? '#f0fdf4' : p.status === 'FAILED' ? '#fef2f2' : p.status === 'REFUNDED' ? '#eff6ff' : '#fffbeb',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    color: p.status === 'COMPLETED' ? '#22c55e' : p.status === 'FAILED' ? '#ef4444' : p.status === 'REFUNDED' ? '#3b82f6' : '#f59e0b',
                  }}>
                    {cfg.icon}
                  </Box>
                  <Box>
                    <Typography fontWeight={600}>{p.amount.toLocaleString()}원</Typography>
                    <Typography variant="body2" color="text.secondary">
                      예약 #{p.bookingId} &middot; {new Date(p.createdAt).toLocaleDateString()}
                    </Typography>
                  </Box>
                </Stack>
                <Stack direction="row" spacing={1} alignItems="center">
                  <Chip label={cfg.label} color={cfg.color} size="small" variant="outlined" />
                  {p.status === 'COMPLETED' && (
                    <Button size="small" variant="outlined" color="warning" startIcon={<Undo />}
                      onClick={() => refundMutation.mutate(p.id)} disabled={refundMutation.isPending}>
                      환불
                    </Button>
                  )}
                </Stack>
              </CardContent>
            </Card>
          )
        })}
      </Stack>
    </Box>
  )
}
