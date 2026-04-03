import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Paper, Typography, Box, Button, Chip, Divider, Stack, Card, CardContent, Alert } from '@mui/material'
import { Videocam, ShoppingCart, History } from '@mui/icons-material'
import { subscriptionApi } from '../../api/subscriptions'
import { requestPayment } from '../../hooks/usePortone'

export default function SubscriptionPage() {
  const queryClient = useQueryClient()
  const [paymentError, setPaymentError] = useState('')

  const { data: subscription } = useQuery({
    queryKey: ['subscription'],
    queryFn: () => subscriptionApi.getMy().then(r => r.data.data),
  })

  const { data: history } = useQuery({
    queryKey: ['subscription', 'history'],
    queryFn: () => subscriptionApi.getPurchaseHistory().then(r => r.data.data),
  })

  const useCallMutation = useMutation({
    mutationFn: () => subscriptionApi.useVideoCall(),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['subscription'] }),
  })

  const purchaseMutation = useMutation({
    mutationFn: async () => {
      const paymentId = await requestPayment(5000, '영상통화 크레딧 10회')
      return subscriptionApi.purchaseCredits({ portonePaymentId: paymentId })
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['subscription'] })
      queryClient.invalidateQueries({ queryKey: ['subscription', 'history'] })
      setPaymentError('')
    },
    onError: (err: Error) => setPaymentError(err.message),
  })

  const handlePurchase = () => {
    setPaymentError('')
    purchaseMutation.mutate()
  }

  return (
    <Paper sx={{ p: 3 }} elevation={0}>
      <Typography variant="h5" fontWeight={600} gutterBottom>구독 관리</Typography>

      {subscription && (
        <Card elevation={0} variant="outlined" sx={{ mb: 3 }}>
          <CardContent>
            <Stack direction="row" justifyContent="space-between" alignItems="center">
              <Box>
                <Typography variant="h6">영상통화 크레딧</Typography>
                <Typography variant="h3" color="primary" fontWeight={700}>
                  {subscription.videoCallCredits}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  총 사용: {subscription.totalVideoCallsUsed}회
                </Typography>
              </Box>
              <Box textAlign="right">
                <Chip label={subscription.status} color={subscription.status === 'ACTIVE' ? 'success' : 'error'} />
              </Box>
            </Stack>
          </CardContent>
        </Card>
      )}

      <Stack direction="row" spacing={2} sx={{ mb: 3 }}>
        <Button variant="contained" startIcon={<Videocam />}
          onClick={() => useCallMutation.mutate()}
          disabled={useCallMutation.isPending}>
          영상통화 사용 (1크레딧)
        </Button>
        <Button variant="outlined" startIcon={<ShoppingCart />}
          onClick={handlePurchase}
          disabled={purchaseMutation.isPending}>
          {purchaseMutation.isPending ? '결제 중...' : '크레딧 충전 (10회 / 5,000원)'}
        </Button>
      </Stack>

      {useCallMutation.isSuccess && (
        <Alert severity={useCallMutation.data?.data.data?.success ? 'success' : 'warning'} sx={{ mb: 2 }}>
          {useCallMutation.data?.data.data?.message}
        </Alert>
      )}

      {paymentError && <Alert severity="error" sx={{ mb: 2 }}>{paymentError}</Alert>}
      {purchaseMutation.isSuccess && <Alert severity="success" sx={{ mb: 2 }}>크레딧 충전이 완료되었습니다!</Alert>}

      <Divider sx={{ my: 3 }} />

      <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <History /> 충전 내역
      </Typography>
      {!history?.length ? (
        <Typography color="text.secondary">충전 내역이 없습니다.</Typography>
      ) : (
        <Stack spacing={1}>
          {history.map(h => (
            <Card key={h.id} elevation={0} variant="outlined">
              <CardContent sx={{ display: 'flex', justifyContent: 'space-between', py: 1.5, '&:last-child': { pb: 1.5 } }}>
                <Typography>{h.credits}회 크레딧</Typography>
                <Stack direction="row" spacing={1} alignItems="center">
                  <Typography>{h.amount.toLocaleString()}원</Typography>
                  <Chip label={h.status} size="small" color={h.status === 'COMPLETED' ? 'success' : 'default'} />
                </Stack>
              </CardContent>
            </Card>
          ))}
        </Stack>
      )}
    </Paper>
  )
}
