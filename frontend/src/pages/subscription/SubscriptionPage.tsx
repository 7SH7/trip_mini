import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Paper, Typography, Box, Button, Chip, Divider, Stack, Card, CardContent, Alert } from '@mui/material'
import { Videocam, ShoppingCart, History } from '@mui/icons-material'
import { subscriptionApi } from '../../api/subscriptions'

export default function SubscriptionPage() {
  const queryClient = useQueryClient()

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
          onClick={() => alert('포트원 결제 연동 후 사용 가능합니다.\n10회 크레딧 / 5,000원')}>
          크레딧 충전 (10회 / 5,000원)
        </Button>
      </Stack>

      {useCallMutation.isSuccess && (
        <Alert severity={useCallMutation.data?.data.data?.success ? 'success' : 'warning'} sx={{ mb: 2 }}>
          {useCallMutation.data?.data.data?.message}
        </Alert>
      )}

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
