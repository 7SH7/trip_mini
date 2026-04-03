import { useState, useRef } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Paper, Typography, Box, Button, TextField, Card, CardContent, Avatar, IconButton, Stack, ImageList, ImageListItem, Skeleton } from '@mui/material'
import { AddPhotoAlternate, Send, Delete } from '@mui/icons-material'
import styled from 'styled-components'
import { feedApi } from '../../api/feeds'
import type { FeedResponse } from '../../types'

const FeedCard = styled(Card)`
  margin-bottom: 1rem;
`

export default function FeedPage() {
  const queryClient = useQueryClient()
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [content, setContent] = useState('')
  const [selectedFiles, setSelectedFiles] = useState<File[]>([])

  const { data: feeds, isLoading } = useQuery({
    queryKey: ['feeds'],
    queryFn: () => feedApi.getAll().then(r => r.data.data),
  })

  const createMutation = useMutation({
    mutationFn: () => feedApi.create(content, selectedFiles.length > 0 ? selectedFiles : undefined),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['feeds'] })
      setContent('')
      setSelectedFiles([])
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id: number) => feedApi.delete(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['feeds'] }),
  })

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) setSelectedFiles(Array.from(e.target.files))
  }

  return (
    <Box>
      <Paper sx={{ p: 3, mb: 3 }} elevation={0}>
        <Typography variant="h5" fontWeight={600} gutterBottom>피드</Typography>
        <Stack spacing={2}>
          <TextField
            multiline rows={3} fullWidth placeholder="무슨 생각을 하고 계신가요?"
            value={content} onChange={e => setContent(e.target.value)}
          />
          <Stack direction="row" justifyContent="space-between" alignItems="center">
            <Box>
              <input ref={fileInputRef} type="file" multiple accept="image/*" hidden onChange={handleFileSelect} />
              <Button startIcon={<AddPhotoAlternate />} onClick={() => fileInputRef.current?.click()}>
                사진 추가 {selectedFiles.length > 0 && `(${selectedFiles.length})`}
              </Button>
            </Box>
            <Button variant="contained" startIcon={<Send />}
              disabled={!content.trim() || createMutation.isPending}
              onClick={() => createMutation.mutate()}>
              {createMutation.isPending ? '게시 중...' : '게시'}
            </Button>
          </Stack>
        </Stack>
      </Paper>

      {isLoading && [1,2,3].map(i => <Skeleton key={i} height={200} variant="rounded" sx={{ mb: 2 }} />)}

      {feeds?.content?.map((feed: FeedResponse) => (
        <FeedCard key={feed.id} elevation={0} variant="outlined">
          <CardContent>
            <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
              <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
                <Avatar sx={{ width: 32, height: 32 }}>{feed.userId}</Avatar>
                <Box>
                  <Typography variant="body2" fontWeight={600}>User #{feed.userId}</Typography>
                  <Typography variant="caption" color="text.secondary">
                    {new Date(feed.createdAt).toLocaleString()}
                  </Typography>
                </Box>
              </Stack>
              <IconButton size="small" onClick={() => deleteMutation.mutate(feed.id)}><Delete fontSize="small" /></IconButton>
            </Stack>
            <Typography sx={{ mb: 1 }}>{feed.content}</Typography>
            {feed.images.length > 0 && (
              <ImageList cols={feed.images.length > 1 ? 2 : 1} gap={8}>
                {feed.images.map(img => (
                  <ImageListItem key={img.id}>
                    <img src={img.imageUrl} alt={img.originalFileName || ''} style={{ borderRadius: 8 }} />
                  </ImageListItem>
                ))}
              </ImageList>
            )}
          </CardContent>
        </FeedCard>
      ))}
    </Box>
  )
}
