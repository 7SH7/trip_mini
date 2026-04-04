import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Typography, Button, Box, Card, CardContent, Stack, Chip, Grid, Divider, Alert } from '@mui/material'
import { VideoCall, ShoppingCart, History, Star } from '@mui/icons-material'
import { subscriptionApi } from '../../api/subscriptions'
import type { CreditPurchaseResponse } from '../../types'
import { requestPayment } from '../../hooks/usePortone'

export default function SubscriptionPage() {
  const queryClient = useQueryClient()
  // requestPayment imported from usePortone module
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null)

  const { data: sub } = useQuery({
    queryKey: ['subscription'],
    queryFn: () => subscriptionApi.getMy().then(r => r.data.data),
  })

  const { data: history } = useQuery({
    queryKey: ['subscription', 'history'],
    queryFn: () => subscriptionApi.getPurchaseHistory().then(r => r.data.data || []),
  })

  const useCallMutation = useMutation({
    mutationFn: () => subscriptionApi.useVideoCall(),
    onSuccess: (res) => {
      queryClient.invalidateQueries({ queryKey: ['subscription'] })
      const data = res.data.data
      setMessage({ type: data?.success ? 'success' : 'error', text: data?.message || '' })
    },
  })

  const purchaseMutation = useMutation({
    mutationFn: async () => {
      const impUid = await requestPayment(5000, '영상통화 크레딧 10회')
      return subscriptionApi.purchaseCredits({ portonePaymentId: impUid })
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['subscription'] })
      queryClient.invalidateQueries({ queryKey: ['subscription', 'history'] })
      setMessage({ type: 'success', text: '크레딧이 충전되었습니다!' })
    },
    onError: () => setMessage({ type: 'error', text: '결제에 실패했습니다.' }),
  })

  return (
    <Box>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4">구독 관리</Typography>
        <Typography variant="body2" color="text.secondary">영상통화 크레딧을 관리하세요</Typography>
      </Box>

      {message && <Alert severity={message.type} onClose={() => setMessage(null)} sx={{ mb: 3, borderRadius: 3 }}>{message.text}</Alert>}

      <Grid container spacing={3} sx={{ mb: 4 }}>
        {/* Credit Balance Card */}
        <Grid size={{ xs: 12, md: 5 }}>
          <Card elevation={0} sx={{
            background: 'linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%)',
            color: 'white', '&:hover': { transform: 'none' },
          }}>
            <CardContent sx={{ p: 3.5 }}>
              <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
                <Box>
                  <Typography variant="body2" sx={{ opacity: 0.8, mb: 0.5 }}>보유 크레딧</Typography>
                  <Typography variant="h3" fontWeight={800}>{sub?.videoCallCredits ?? 5}</Typography>
                  <Typography variant="body2" sx={{ opacity: 0.7, mt: 1 }}>
                    총 {sub?.totalVideoCallsUsed ?? 0}회 사용
                  </Typography>
                </Box>
                <Box sx={{ width: 56, height: 56, borderRadius: '16px', bgcolor: 'rgba(255,255,255,0.2)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <Star sx={{ fontSize: 28 }} />
                </Box>
              </Stack>
              {sub?.status && (
                <Chip label={sub.status === 'ACTIVE' ? '활성' : '정지'} size="small"
                  sx={{ mt: 2, bgcolor: 'rgba(255,255,255,0.25)', color: 'white', fontWeight: 700 }} />
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Action Cards */}
        <Grid size={{ xs: 12, md: 7 }}>
          <Stack spacing={2}>
            <Card elevation={0}>
              <CardContent sx={{ p: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center', '&:last-child': { pb: 3 } }}>
                <Stack direction="row" spacing={2} alignItems="center">
                  <Box sx={{ width: 48, height: 48, borderRadius: '12px', bgcolor: '#eff6ff', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <VideoCall sx={{ color: '#3b82f6' }} />
                  </Box>
                  <Box>
                    <Typography fontWeight={600}>영상통화 사용</Typography>
                    <Typography variant="body2" color="text.secondary">크레딧 1개 차감</Typography>
                  </Box>
                </Stack>
                <Button variant="contained" disableElevation onClick={() => useCallMutation.mutate()}
                  disabled={useCallMutation.isPending}
                  sx={{ background: 'linear-gradient(135deg, #3b82f6, #2563eb)' }}>
                  사용하기
                </Button>
              </CardContent>
            </Card>

            <Card elevation={0}>
              <CardContent sx={{ p: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center', '&:last-child': { pb: 3 } }}>
                <Stack direction="row" spacing={2} alignItems="center">
                  <Box sx={{ width: 48, height: 48, borderRadius: '12px', bgcolor: '#fff7ed', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                    <ShoppingCart sx={{ color: '#f97316' }} />
                  </Box>
                  <Box>
                    <Typography fontWeight={600}>크레딧 충전</Typography>
                    <Typography variant="body2" color="text.secondary">10회 / 5,000원</Typography>
                  </Box>
                </Stack>
                <Button variant="contained" color="secondary" disableElevation
                  onClick={() => purchaseMutation.mutate()} disabled={purchaseMutation.isPending}>
                  충전하기
                </Button>
              </CardContent>
            </Card>
          </Stack>
        </Grid>
      </Grid>

      {/* Purchase History */}
      {history && history.length > 0 && (
        <Box>
          <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 2 }}>
            <History sx={{ color: 'text.secondary' }} />
            <Typography variant="h6">충전 내역</Typography>
          </Stack>
          <Card elevation={0}>
            {history.map((h: CreditPurchaseResponse, i: number) => (
              <Box key={h.id}>
                <CardContent sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', py: 2, '&:last-child': { pb: 2 } }}>
                  <Box>
                    <Typography fontWeight={600}>{h.credits}회 크레딧</Typography>
                    <Typography variant="body2" color="text.secondary">{h.amount.toLocaleString()}원</Typography>
                  </Box>
                  <Chip label={h.status === 'COMPLETED' ? '완료' : h.status === 'FAILED' ? '실패' : '대기'}
                    color={h.status === 'COMPLETED' ? 'success' : h.status === 'FAILED' ? 'error' : 'warning'}
                    size="small" variant="outlined" />
                </CardContent>
                {i < history.length - 1 && <Divider />}
              </Box>
            ))}
          </Card>
        </Box>
      )}
    </Box>
  )
}
