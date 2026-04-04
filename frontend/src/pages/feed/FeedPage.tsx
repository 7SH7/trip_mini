import { useState, useRef } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Typography, TextField, Button, Box, Card, CardContent, Stack, Avatar, IconButton, Skeleton, ImageList, ImageListItem } from '@mui/material'
import { Send, AddPhotoAlternate, Delete, Feed, Image as ImageIcon } from '@mui/icons-material'
import { feedApi } from '../../api/feeds'
import type { FeedResponse } from '../../types'
import { useAppSelector } from '../../store/hooks'
import { motion } from 'framer-motion'
import styled from '@emotion/styled'

const FeedCard = styled(motion.div)`
  margin-bottom: 16px;
`

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
    <Box sx={{ maxWidth: 600, mx: 'auto' }}>
      <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.4 }}>
        <Box sx={{ mb: 4 }}>
          <Typography variant="h4">피드</Typography>
          <Typography variant="body2" color="text.secondary">여행 이야기를 공유하세요</Typography>
        </Box>

        {/* Create Post */}
        <Card elevation={0} sx={{ mb: 4, '&:hover': { transform: 'none' }, border: '1px solid #e2e8f0' }}>
          <CardContent sx={{ p: 3 }}>
            <Stack direction="row" spacing={2}>
              <Avatar sx={{
                width: 42, height: 42, fontWeight: 700,
                background: 'linear-gradient(135deg, #3b82f6, #8b5cf6)',
              }}>
                {user?.name?.charAt(0) || 'U'}
              </Avatar>
              <Box sx={{ flex: 1 }}>
                <TextField fullWidth multiline minRows={2} maxRows={5} placeholder="무슨 생각을 하고 계신가요?"
                  value={content} onChange={e => setContent(e.target.value)}
                  variant="standard"
                  sx={{ mb: 1.5, '& .MuiInput-root': { fontSize: '0.95rem', '&:before': { borderColor: '#f1f5f9' } } }} />
                {files && files.length > 0 && (
                  <Box sx={{ mb: 1.5, p: 1.5, bgcolor: '#f8fafc', borderRadius: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
                    <ImageIcon sx={{ fontSize: 18, color: '#3b82f6' }} />
                    <Typography variant="caption" color="text.secondary">{files.length}개 파일 선택됨</Typography>
                    <Button size="small" onClick={() => setFiles(null)} sx={{ ml: 'auto', minWidth: 0, color: '#94a3b8' }}>취소</Button>
                  </Box>
                )}
                <Stack direction="row" justifyContent="space-between" alignItems="center">
                  <input ref={fileRef} type="file" accept="image/*" multiple hidden onChange={e => setFiles(e.target.files)} />
                  <IconButton onClick={() => fileRef.current?.click()}
                    sx={{ color: '#94a3b8', '&:hover': { color: '#3b82f6', bgcolor: '#eff6ff' } }}>
                    <AddPhotoAlternate />
                  </IconButton>
                  <Button variant="contained" size="small" disableElevation startIcon={<Send />}
                    onClick={() => createMutation.mutate()} disabled={!content.trim() || createMutation.isPending}
                    sx={{ background: 'linear-gradient(135deg, #3b82f6, #8b5cf6)', borderRadius: '10px', px: 2.5 }}>
                    게시
                  </Button>
                </Stack>
              </Box>
            </Stack>
          </CardContent>
        </Card>
      </motion.div>

      {isLoading && <Stack spacing={2}>{[1,2].map(i => <Skeleton key={i} height={200} variant="rounded" sx={{ borderRadius: 4 }} />)}</Stack>}

      {!data?.content?.length && !isLoading && (
        <Box textAlign="center" py={8} sx={{ bgcolor: '#f8fafc', borderRadius: 4, border: '2px dashed #e2e8f0' }}>
          <Feed sx={{ fontSize: 56, color: '#cbd5e1', mb: 2 }} />
          <Typography variant="h6" color="text.secondary">아직 게시물이 없습니다</Typography>
          <Typography variant="body2" color="text.secondary">첫 게시물을 작성해보세요</Typography>
        </Box>
      )}

      {data?.content?.map((feed: FeedResponse, idx: number) => (
        <FeedCard key={feed.id}
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: idx * 0.05, duration: 0.4 }}>
          <Card elevation={0} sx={{ '&:hover': { transform: 'none' }, border: '1px solid #e2e8f0' }}>
            <CardContent sx={{ p: 3 }}>
              <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
                <Stack direction="row" spacing={1.5} alignItems="center">
                  <Avatar sx={{
                    width: 38, height: 38, fontSize: 13, fontWeight: 700,
                    background: `linear-gradient(135deg, hsl(${feed.userId * 40}, 70%, 60%), hsl(${feed.userId * 40 + 40}, 70%, 50%))`,
                  }}>
                    {feed.userId}
                  </Avatar>
                  <Box>
                    <Typography variant="body2" fontWeight={600}>User #{feed.userId}</Typography>
                    <Typography variant="caption" sx={{ color: '#94a3b8', fontSize: '0.7rem' }}>{new Date(feed.createdAt).toLocaleString()}</Typography>
                  </Box>
                </Stack>
                {user?.id === feed.userId && (
                  <IconButton size="small" onClick={() => deleteMutation.mutate(feed.id)}
                    sx={{ color: '#cbd5e1', '&:hover': { color: '#ef4444', bgcolor: '#fef2f2' } }}>
                    <Delete fontSize="small" />
                  </IconButton>
                )}
              </Stack>

              <Typography variant="body1" sx={{ mb: feed.images?.length ? 2 : 0, whiteSpace: 'pre-wrap', lineHeight: 1.7, color: '#334155' }}>
                {feed.content}
              </Typography>

              {feed.images?.length > 0 && (
                <ImageList cols={feed.images.length === 1 ? 1 : 2} gap={6}
                  sx={{ mt: 0, borderRadius: 3, overflow: 'hidden' }}>
                  {feed.images.map(img => (
                    <ImageListItem key={img.id}>
                      <img src={img.imageUrl} alt={img.originalFileName || ''} loading="lazy"
                        style={{ borderRadius: 10, objectFit: 'cover', maxHeight: 320 }} />
                    </ImageListItem>
                  ))}
                </ImageList>
              )}
            </CardContent>
          </Card>
        </FeedCard>
      ))}
    </Box>
  )
}
