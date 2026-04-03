import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Paper, Typography, Box, Button, TextField, Card, CardContent, Chip, Stack, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material'
import { LiveTv, Add, PlayArrow, ContentCopy } from '@mui/icons-material'
import { mediaApi } from '../../api/media'
import type { LiveStreamResponse } from '../../types'

const statusLabel: Record<string, string> = { IDLE: '대기', LIVE: '방송 중', ENDED: '종료' }
const statusColor: Record<string, 'default' | 'error' | 'success'> = { IDLE: 'default', LIVE: 'error', ENDED: 'default' }

export default function LiveStreamsPage() {
  const queryClient = useQueryClient()
  const [open, setOpen] = useState(false)
  const [title, setTitle] = useState('')

  const { data: activeStreams, isLoading } = useQuery({
    queryKey: ['streams', 'active'],
    queryFn: () => mediaApi.getActiveStreams().then(r => r.data.data),
    refetchInterval: 10000,
  })

  const { data: myStreams } = useQuery({
    queryKey: ['streams', 'my'],
    queryFn: () => mediaApi.getMyStreams().then(r => r.data.data),
  })

  const createMutation = useMutation({
    mutationFn: () => mediaApi.createStream(title),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['streams'] })
      setOpen(false)
      setTitle('')
    },
  })

  const StreamCard = ({ stream }: { stream: LiveStreamResponse }) => (
    <Card elevation={0} variant="outlined">
      <CardContent sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Stack direction="row" spacing={1} alignItems="center">
            <Typography fontWeight={600}>{stream.title}</Typography>
            <Chip label={statusLabel[stream.status]} color={statusColor[stream.status]} size="small"
              icon={stream.status === 'LIVE' ? <PlayArrow /> : undefined} />
          </Stack>
          <Typography variant="body2" color="text.secondary">
            User #{stream.userId} | {new Date(stream.createdAt).toLocaleString()}
          </Typography>
          {stream.rtmpIngestUrl && (
            <Typography variant="caption" color="text.secondary" sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mt: 0.5 }}>
              RTMP: {stream.rtmpIngestUrl}
              <IconButtonCopy text={stream.rtmpIngestUrl} />
            </Typography>
          )}
        </Box>
        {stream.hlsPlaybackUrl && stream.status === 'LIVE' && (
          <Button variant="outlined" size="small" href={stream.hlsPlaybackUrl} target="_blank">
            시청하기
          </Button>
        )}
      </CardContent>
    </Card>
  )

  return (
    <Paper sx={{ p: 3 }} elevation={0}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Typography variant="h5" fontWeight={600}>실시간 스트리밍</Typography>
        <Button variant="contained" startIcon={<Add />} onClick={() => setOpen(true)}>스트림 생성</Button>
      </Stack>

      <Typography variant="h6" gutterBottom>라이브 방송</Typography>
      {isLoading && <Typography color="text.secondary">로딩 중...</Typography>}
      {!activeStreams?.length && !isLoading && (
        <Box textAlign="center" py={3} color="#999">
          <LiveTv sx={{ fontSize: 48, mb: 1 }} />
          <Typography>현재 라이브 방송이 없습니다.</Typography>
        </Box>
      )}
      <Stack spacing={1} sx={{ mb: 3 }}>
        {activeStreams?.map((s: LiveStreamResponse) => <StreamCard key={s.id} stream={s} />)}
      </Stack>

      {myStreams && myStreams.length > 0 && (
        <>
          <Typography variant="h6" gutterBottom>내 스트림</Typography>
          <Stack spacing={1}>
            {myStreams.map((s: LiveStreamResponse) => <StreamCard key={s.id} stream={s} />)}
          </Stack>
        </>
      )}

      <Dialog open={open} onClose={() => setOpen(false)}>
        <DialogTitle>새 스트림 생성</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus fullWidth label="방송 제목" sx={{ mt: 1 }}
            value={title} onChange={e => setTitle(e.target.value)}
          />
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            생성 후 RTMP URL과 스트림 키를 받아 OBS 등에서 송출하세요.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>취소</Button>
          <Button variant="contained" onClick={() => createMutation.mutate()} disabled={!title.trim()}>생성</Button>
        </DialogActions>
      </Dialog>
    </Paper>
  )
}

function IconButtonCopy({ text }: { text: string }) {
  return (
    <ContentCopy
      sx={{ fontSize: 14, cursor: 'pointer', '&:hover': { color: 'primary.main' } }}
      onClick={() => navigator.clipboard.writeText(text)}
    />
  )
}
