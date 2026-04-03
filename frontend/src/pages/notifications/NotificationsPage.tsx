import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Paper, Typography, Box, Button, Card, CardContent, Chip, Stack, Skeleton } from '@mui/material'
import { Notifications, MarkEmailRead, Circle } from '@mui/icons-material'
import { notificationApi } from '../../api/notifications'
import type { NotificationResponse } from '../../types'

const typeLabel: Record<string, string> = {
  BOOKING_CONFIRMED: '예약 확정',
  BOOKING_CANCELLED: '예약 취소',
  PAYMENT_COMPLETED: '결제 완료',
  PAYMENT_REFUNDED: '환불 완료',
  CHAT_MESSAGE: '채팅',
  SYSTEM: '시스템',
}

const typeColor: Record<string, 'success' | 'error' | 'info' | 'warning' | 'default'> = {
  BOOKING_CONFIRMED: 'success',
  BOOKING_CANCELLED: 'error',
  PAYMENT_COMPLETED: 'info',
  PAYMENT_REFUNDED: 'warning',
  CHAT_MESSAGE: 'default',
  SYSTEM: 'default',
}

export default function NotificationsPage() {
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['notifications'],
    queryFn: () => notificationApi.getAll().then(r => r.data.data),
  })

  const markReadMutation = useMutation({
    mutationFn: (id: number) => notificationApi.markAsRead(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] })
      queryClient.invalidateQueries({ queryKey: ['notifications', 'unread'] })
    },
  })

  const markAllMutation = useMutation({
    mutationFn: () => notificationApi.markAllAsRead(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] })
      queryClient.invalidateQueries({ queryKey: ['notifications', 'unread'] })
    },
  })

  return (
    <Paper sx={{ p: 3 }} elevation={0}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Typography variant="h5" fontWeight={600}>알림</Typography>
        <Button startIcon={<MarkEmailRead />} onClick={() => markAllMutation.mutate()}>
          모두 읽음
        </Button>
      </Stack>

      {isLoading && [1,2,3].map(i => <Skeleton key={i} height={80} variant="rounded" sx={{ mb: 1 }} />)}

      {!data?.content?.length && !isLoading && (
        <Box textAlign="center" py={5} color="#999">
          <Notifications sx={{ fontSize: 48, mb: 1 }} />
          <Typography>알림이 없습니다.</Typography>
        </Box>
      )}

      <Stack spacing={1}>
        {data?.content?.map((n: NotificationResponse) => (
          <Card key={n.id} elevation={0} variant="outlined"
            sx={{ cursor: n.isRead ? 'default' : 'pointer', bgcolor: n.isRead ? 'transparent' : '#f0f7ff' }}
            onClick={() => !n.isRead && markReadMutation.mutate(n.id)}>
            <CardContent sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', py: 1.5, '&:last-child': { pb: 1.5 } }}>
              <Stack direction="row" spacing={1.5} alignItems="center">
                {!n.isRead && <Circle sx={{ fontSize: 8, color: 'primary.main' }} />}
                <Box>
                  <Stack direction="row" spacing={1} alignItems="center">
                    <Typography fontWeight={n.isRead ? 400 : 600}>{n.title}</Typography>
                    <Chip label={typeLabel[n.type] || n.type} size="small" color={typeColor[n.type] || 'default'} />
                  </Stack>
                  <Typography variant="body2" color="text.secondary">{n.content}</Typography>
                </Box>
              </Stack>
              <Typography variant="caption" color="text.secondary">
                {new Date(n.createdAt).toLocaleString()}
              </Typography>
            </CardContent>
          </Card>
        ))}
      </Stack>
    </Paper>
  )
}
