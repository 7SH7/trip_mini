import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Typography, Button, Box, Card, CardContent, Stack, Chip, Grid,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField, IconButton, Tooltip
} from '@mui/material'
import { Add, LiveTv, PlayCircle, ContentCopy, FiberManualRecord } from '@mui/icons-material'
import { mediaApi } from '../../api/media'
import type { LiveStreamResponse } from '../../types'
import HlsPlayer from '../../components/common/HlsPlayer'

export default function LiveStreamsPage() {
  const [open, setOpen] = useState(false)
  const [title, setTitle] = useState('')
  const [copied, setCopied] = useState<number | null>(null)
  const queryClient = useQueryClient()

  const { data: activeStreams } = useQuery({
    queryKey: ['streams', 'active'],
    queryFn: () => mediaApi.getActiveStreams().then(r => r.data.data || []),
    refetchInterval: 10000,
  })

  const { data: myStreams } = useQuery({
    queryKey: ['streams', 'my'],
    queryFn: () => mediaApi.getMyStreams().then(r => r.data.data || []),
  })

  const createMutation = useMutation({
    mutationFn: () => mediaApi.createStream(title),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['streams'] })
      setOpen(false); setTitle('')
    },
  })

  const handleCopy = (text: string, id: number) => {
    navigator.clipboard.writeText(text)
    setCopied(id)
    setTimeout(() => setCopied(null), 2000)
  }

  const statusConfig: Record<string, { color: string; bg: string; label: string }> = {
    IDLE: { color: '#64748b', bg: '#f1f5f9', label: '대기' },
    LIVE: { color: '#ef4444', bg: '#fef2f2', label: 'LIVE' },
    ENDED: { color: '#94a3b8', bg: '#f8fafc', label: '종료' },
  }

  const StreamCard = ({ stream }: { stream: LiveStreamResponse }) => {
    const cfg = statusConfig[stream.status] || statusConfig.IDLE
    return (
      <Card elevation={0} sx={{ '&:hover': { transform: stream.status === 'LIVE' ? 'none' : undefined } }}>
        {stream.status === 'LIVE' && stream.hlsPlaybackUrl && (
          <Box sx={{ borderRadius: '16px 16px 0 0', overflow: 'hidden' }}>
            <HlsPlayer src={stream.hlsPlaybackUrl} />
          </Box>
        )}
        {stream.status !== 'LIVE' && (
          <Box sx={{
            height: 140, display: 'flex', alignItems: 'center', justifyContent: 'center',
            bgcolor: cfg.bg, borderRadius: '16px 16px 0 0',
          }}>
            {stream.status === 'IDLE' ? <LiveTv sx={{ fontSize: 48, color: '#cbd5e1' }} /> :
              <PlayCircle sx={{ fontSize: 48, color: '#cbd5e1' }} />}
          </Box>
        )}
        <CardContent sx={{ p: 2.5 }}>
          <Stack direction="row" justifyContent="space-between" alignItems="flex-start" sx={{ mb: 1 }}>
            <Typography fontWeight={700} sx={{ fontSize: '1.05rem' }}>{stream.title}</Typography>
            <Chip
              icon={stream.status === 'LIVE' ? <FiberManualRecord sx={{ fontSize: '10px !important', color: '#ef4444 !important' }} /> : undefined}
              label={cfg.label} size="small"
              sx={{ bgcolor: cfg.bg, color: cfg.color, fontWeight: 700 }} />
          </Stack>
          <Typography variant="caption" color="text.secondary">
            User #{stream.userId} &middot; {new Date(stream.createdAt).toLocaleDateString()}
          </Typography>

          {stream.rtmpIngestUrl && (
            <Box sx={{ mt: 1.5, p: 1.5, bgcolor: '#f8fafc', borderRadius: 2, border: '1px solid #e2e8f0' }}>
              <Stack direction="row" justifyContent="space-between" alignItems="center">
                <Box sx={{ minWidth: 0 }}>
                  <Typography variant="caption" color="text.secondary">RTMP URL</Typography>
                  <Typography variant="body2" fontFamily="monospace" noWrap sx={{ fontSize: '0.8rem' }}>
                    {stream.rtmpIngestUrl}
                  </Typography>
                </Box>
                <Tooltip title={copied === stream.id ? '복사됨!' : '복사'}>
                  <IconButton size="small" onClick={() => handleCopy(stream.rtmpIngestUrl!, stream.id)}>
                    <ContentCopy fontSize="small" />
                  </IconButton>
                </Tooltip>
              </Stack>
            </Box>
          )}
        </CardContent>
      </Card>
    )
  }

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 4 }}>
        <Box>
          <Typography variant="h4">라이브 스트리밍</Typography>
          <Typography variant="body2" color="text.secondary">실시간 여행 방송을 시청하거나 직접 시작하세요</Typography>
        </Box>
        <Button variant="contained" startIcon={<Add />} onClick={() => setOpen(true)} disableElevation
          sx={{ background: 'linear-gradient(135deg, #ef4444, #f97316)' }}>
          방송 시작
        </Button>
      </Stack>

      {/* Active Streams */}
      {activeStreams && activeStreams.length > 0 && (
        <Box sx={{ mb: 5 }}>
          <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 2 }}>
            <FiberManualRecord sx={{ fontSize: 12, color: '#ef4444' }} />
            <Typography variant="h6">라이브 방송</Typography>
            <Chip label={`${activeStreams.length}개`} size="small" color="error" />
          </Stack>
          <Grid container spacing={2.5}>
            {activeStreams.map((s: LiveStreamResponse) => (
              <Grid size={{ xs: 12, sm: 6, md: 4 }} key={s.id}><StreamCard stream={s} /></Grid>
            ))}
          </Grid>
        </Box>
      )}

      {/* My Streams */}
      <Box>
        <Typography variant="h6" sx={{ mb: 2 }}>내 스트림</Typography>
        {!myStreams?.length ? (
          <Box textAlign="center" py={6} sx={{ bgcolor: '#f8fafc', borderRadius: 4, border: '2px dashed #e2e8f0' }}>
            <LiveTv sx={{ fontSize: 56, color: '#cbd5e1', mb: 2 }} />
            <Typography variant="h6" color="text.secondary">스트림이 없습니다</Typography>
            <Typography variant="body2" color="text.secondary">새 방송을 시작해보세요</Typography>
          </Box>
        ) : (
          <Grid container spacing={2.5}>
            {myStreams.map((s: LiveStreamResponse) => (
              <Grid size={{ xs: 12, sm: 6, md: 4 }} key={s.id}><StreamCard stream={s} /></Grid>
            ))}
          </Grid>
        )}
      </Box>

      {/* Create Dialog */}
      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="xs" fullWidth PaperProps={{ sx: { borderRadius: 3 } }}>
        <DialogTitle sx={{ fontWeight: 700 }}>새 방송 만들기</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            생성 후 RTMP URL을 OBS 등에서 설정하여 송출하세요
          </Typography>
          <TextField fullWidth label="방송 제목" value={title} onChange={e => setTitle(e.target.value)}
            autoFocus placeholder="예: 제주도 일출 라이브" />
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setOpen(false)} sx={{ color: 'text.secondary' }}>취소</Button>
          <Button variant="contained" onClick={() => createMutation.mutate()} disabled={!title.trim() || createMutation.isPending}
            disableElevation sx={{ background: 'linear-gradient(135deg, #ef4444, #f97316)' }}>
            만들기
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
