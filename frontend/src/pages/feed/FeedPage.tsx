import { useState, useRef } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Typography, TextField, Button, Box, Card, CardContent, Stack, Avatar, IconButton, Skeleton, ImageList, ImageListItem } from '@mui/material'
import { Send, AddPhotoAlternate, Delete, Feed } from '@mui/icons-material'
import { feedApi } from '../../api/feeds'
import type { FeedResponse } from '../../types'
import { useAppSelector } from '../../store/hooks'

export default function FeedPage() {
  const [content, setContent] = useState('')
  const [files, setFiles] = useState<FileList | null>(null)
  const fileRef = useRef<HTMLInputElement>(null)
  const queryClient = useQueryClient()
  const { user } = useAppSelector(state => state.auth)

  const { data, isLoading } = useQuery({
    queryKey: ['feeds'],
    queryFn: () => feedApi.getAll().then(r => r.data.data),
  })

  const createMutation = useMutation({
    mutationFn: () => feedApi.create(content, files ? Array.from(files) : undefined),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['feeds'] }); setContent(''); setFiles(null) },
  })

  const deleteMutation = useMutation({
    mutationFn: (id: number) => feedApi.delete(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['feeds'] }),
  })

  return (
    <Box sx={{ maxWidth: 640, mx: 'auto' }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4">피드</Typography>
        <Typography variant="body2" color="text.secondary">여행 이야기를 공유하세요</Typography>
      </Box>

      {/* Create Post */}
      <Card elevation={0} sx={{ mb: 4, '&:hover': { transform: 'none' } }}>
        <CardContent sx={{ p: 3 }}>
          <Stack direction="row" spacing={2}>
            <Avatar sx={{ width: 40, height: 40, bgcolor: 'primary.main', fontWeight: 700 }}>
              {user?.name?.charAt(0) || 'U'}
            </Avatar>
            <Box sx={{ flex: 1 }}>
              <TextField fullWidth multiline minRows={2} maxRows={5} placeholder="무슨 생각을 하고 계신가요?"
                value={content} onChange={e => setContent(e.target.value)}
                variant="outlined" sx={{ mb: 1.5, '& .MuiOutlinedInput-root': { borderRadius: 3 } }} />
              {files && files.length > 0 && (
                <Typography variant="caption" color="text.secondary" sx={{ mb: 1, display: 'block' }}>
                  {files.length}개 파일 선택됨
                </Typography>
              )}
              <Stack direction="row" justifyContent="space-between" alignItems="center">
                <input ref={fileRef} type="file" accept="image/*" multiple hidden onChange={e => setFiles(e.target.files)} />
                <IconButton onClick={() => fileRef.current?.click()} sx={{ color: 'text.secondary' }}>
                  <AddPhotoAlternate />
                </IconButton>
                <Button variant="contained" size="small" disableElevation startIcon={<Send />}
                  onClick={() => createMutation.mutate()} disabled={!content.trim() || createMutation.isPending}
                  sx={{ background: 'linear-gradient(135deg, #3b82f6, #2563eb)' }}>
                  게시
                </Button>
              </Stack>
            </Box>
          </Stack>
        </CardContent>
      </Card>

      {/* Feed List */}
      {isLoading && <Stack spacing={2}>{[1,2].map(i => <Skeleton key={i} height={200} variant="rounded" sx={{ borderRadius: 4 }} />)}</Stack>}

      {!data?.content?.length && !isLoading && (
        <Box textAlign="center" py={8} sx={{ bgcolor: '#f8fafc', borderRadius: 4, border: '2px dashed #e2e8f0' }}>
          <Feed sx={{ fontSize: 56, color: '#cbd5e1', mb: 2 }} />
          <Typography variant="h6" color="text.secondary">아직 게시물이 없습니다</Typography>
          <Typography variant="body2" color="text.secondary">첫 게시물을 작성해보세요</Typography>
        </Box>
      )}

      <Stack spacing={2}>
        {data?.content?.map((feed: FeedResponse) => (
          <Card key={feed.id} elevation={0} sx={{ '&:hover': { transform: 'none' } }}>
            <CardContent sx={{ p: 3 }}>
              <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
                <Stack direction="row" spacing={1.5} alignItems="center">
                  <Avatar sx={{ width: 36, height: 36, bgcolor: '#e2e8f0', color: 'text.secondary', fontSize: 14, fontWeight: 700 }}>
                    {feed.userId}
                  </Avatar>
                  <Box>
                    <Typography variant="body2" fontWeight={600}>User #{feed.userId}</Typography>
                    <Typography variant="caption" color="text.secondary">{new Date(feed.createdAt).toLocaleString()}</Typography>
                  </Box>
                </Stack>
                {user?.id === feed.userId && (
                  <IconButton size="small" onClick={() => deleteMutation.mutate(feed.id)} sx={{ color: 'text.secondary', '&:hover': { color: '#ef4444' } }}>
                    <Delete fontSize="small" />
                  </IconButton>
                )}
              </Stack>

              <Typography variant="body1" sx={{ mb: feed.images?.length ? 2 : 0, whiteSpace: 'pre-wrap', lineHeight: 1.7 }}>
                {feed.content}
              </Typography>

              {feed.images?.length > 0 && (
                <ImageList cols={feed.images.length === 1 ? 1 : 2} gap={8} sx={{ mt: 0, borderRadius: 3, overflow: 'hidden' }}>
                  {feed.images.map(img => (
                    <ImageListItem key={img.id}>
                      <img src={img.imageUrl} alt={img.originalFileName || ''} loading="lazy"
                        style={{ borderRadius: 8, objectFit: 'cover', maxHeight: 300 }} />
                    </ImageListItem>
                  ))}
                </ImageList>
              )}
            </CardContent>
          </Card>
        ))}
      </Stack>
    </Box>
  )
}
