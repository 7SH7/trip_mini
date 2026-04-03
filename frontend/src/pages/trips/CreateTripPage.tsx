import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Paper, TextField, Button, Typography, Stack, Alert, Box } from '@mui/material'
import { tripApi } from '../../api/trips'

export default function CreateTripPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')

  const mutation = useMutation({
    mutationFn: () => tripApi.create({ title, description: description || undefined, startDate, endDate }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['trips'] })
      navigate('/trips')
    },
  })

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    mutation.mutate()
  }

  return (
    <Paper sx={{ p: 3, maxWidth: 500, mx: 'auto' }} elevation={0}>
      <Typography variant="h5" fontWeight={600} gutterBottom>새 여행 만들기</Typography>
      <Box component="form" onSubmit={handleSubmit}>
        <Stack spacing={2}>
          <TextField label="제목" value={title} onChange={e => setTitle(e.target.value)} required fullWidth />
          <TextField label="설명" value={description} onChange={e => setDescription(e.target.value)} fullWidth multiline rows={3} />
          <TextField label="시작일" type="date" value={startDate} onChange={e => setStartDate(e.target.value)} required fullWidth slotProps={{ inputLabel: { shrink: true } }} />
          <TextField label="종료일" type="date" value={endDate} onChange={e => setEndDate(e.target.value)} required fullWidth slotProps={{ inputLabel: { shrink: true } }} />
          {mutation.isError && <Alert severity="error">여행 생성에 실패했습니다.</Alert>}
          <Stack direction="row" spacing={1}>
            <Button type="submit" variant="contained" disabled={mutation.isPending}>
              {mutation.isPending ? '생성 중...' : '만들기'}
            </Button>
            <Button variant="outlined" onClick={() => navigate('/trips')}>취소</Button>
          </Stack>
        </Stack>
      </Box>
    </Paper>
  )
}
