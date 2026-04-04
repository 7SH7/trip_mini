import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Typography, Box, Button, Card, CardContent, Chip, Stack, Skeleton } from '@mui/material'
import {
  Notifications, MarkEmailRead, Circle, CheckCircle, Cancel,
  Payment, Undo, Chat, Settings, Error
} from '@mui/icons-material'
import { notificationApi } from '../../api/notifications'
import type { NotificationResponse } from '../../types'

const typeConfig: Record<string, { label: string; color: 'success' | 'error' | 'info' | 'warning' | 'default'; icon: React.ReactNode }> = {
  BOOKING_CONFIRMED: { label: '예약 확정', color: 'success', icon: <CheckCircle sx={{ fontSize: 20 }} /> },
  BOOKING_CANCELLED: { label: '예약 취소', color: 'error', icon: <Cancel sx={{ fontSize: 20 }} /> },
  PAYMENT_COMPLETED: { label: '결제 완료', color: 'info', icon: <Payment sx={{ fontSize: 20 }} /> },
  PAYMENT_FAILED: { label: '결제 실패', color: 'error', icon: <Error sx={{ fontSize: 20 }} /> },
  PAYMENT_REFUNDED: { label: '환불 완료', color: 'warning', icon: <Undo sx={{ fontSize: 20 }} /> },
  CHAT_MESSAGE: { label: '채팅', color: 'default', icon: <Chat sx={{ fontSize: 20 }} /> },
  SYSTEM: { label: '시스템', color: 'default', icon: <Settings sx={{ fontSize: 20 }} /> },
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
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Box>
          <Typography variant="h4">알림</Typography>
          <Typography variant="body2" color="text.secondary">활동 알림을 확인하세요</Typography>
        </Box>
        <Button startIcon={<MarkEmailRead />} onClick={() => markAllMutation.mutate()} variant="outlined"
          sx={{ borderColor: '#e2e8f0', color: 'text.secondary' }}>
          모두 읽음
        </Button>
      </Stack>

      {isLoading && <Stack spacing={1.5}>{[1,2,3].map(i => <Skeleton key={i} height={80} variant="rounded" sx={{ borderRadius: 4 }} />)}</Stack>}

      {!data?.content?.length && !isLoading && (
        <Box textAlign="center" py={8} sx={{ bgcolor: '#f8fafc', borderRadius: 4, border: '2px dashed #e2e8f0' }}>
          <Notifications sx={{ fontSize: 56, color: '#cbd5e1', mb: 2 }} />
          <Typography variant="h6" color="text.secondary">알림이 없습니다</Typography>
        </Box>
      )}

      <Stack spacing={1}>
        {data?.content?.map((n: NotificationResponse) => {
          const cfg = typeConfig[n.type] || typeConfig.SYSTEM
          return (
            <Card key={n.id} elevation={0}
              sx={{
                cursor: n.isRead ? 'default' : 'pointer',
                bgcolor: n.isRead ? 'white' : '#f0f7ff',
                borderLeft: n.isRead ? 'none' : '3px solid #3b82f6',
                '&:hover': { transform: 'none', boxShadow: n.isRead ? undefined : '0 4px 12px rgba(59,130,246,0.1)' },
              }}
              onClick={() => !n.isRead && markReadMutation.mutate(n.id)}>
              <CardContent sx={{ display: 'flex', gap: 2, py: 2, '&:last-child': { pb: 2 } }}>
                <Box sx={{
                  width: 40, height: 40, borderRadius: '10px', flexShrink: 0,
                  bgcolor: n.isRead ? '#f1f5f9' : '#eff6ff',
                  color: n.isRead ? '#94a3b8' : '#3b82f6',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                }}>
                  {cfg.icon}
                </Box>
                <Box sx={{ flex: 1, minWidth: 0 }}>
                  <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 0.3 }}>
                    {!n.isRead && <Circle sx={{ fontSize: 8, color: 'primary.main' }} />}
                    <Typography fontWeight={n.isRead ? 400 : 600} variant="body2">{n.title}</Typography>
                    <Chip label={cfg.label} size="small" color={cfg.color} variant="outlined" sx={{ height: 22, fontSize: '0.7rem' }} />
                  </Stack>
                  <Typography variant="body2" color="text.secondary" noWrap>{n.content}</Typography>
                </Box>
                <Typography variant="caption" color="text.secondary" sx={{ flexShrink: 0, whiteSpace: 'nowrap' }}>
                  {new Date(n.createdAt).toLocaleDateString()}
                </Typography>
              </CardContent>
            </Card>
          )
        })}
      </Stack>
    </Box>
  )
}
