import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Typography, TextField, Button, Alert, Box, Paper, Stack } from '@mui/material'
import { ArrowBack, Flight } from '@mui/icons-material'
import { tripApi } from '../../api/trips'

export default function CreateTripPage() {
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const mutation = useMutation({
    mutationFn: () => tripApi.create({ title, description, startDate, endDate }),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['trips'] }); navigate('/trips') },
  })

  return (
    <Box sx={{ maxWidth: 560, mx: 'auto' }}>
      <Button startIcon={<ArrowBack />} onClick={() => navigate('/trips')} sx={{ mb: 2, color: 'text.secondary' }}>
        돌아가기
      </Button>
      <Paper elevation={0} sx={{ p: 4, border: '1px solid #e2e8f0' }}>
        <Stack direction="row" spacing={1.5} alignItems="center" sx={{ mb: 3 }}>
          <Box sx={{ width: 44, height: 44, borderRadius: '12px', bgcolor: '#eff6ff', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Flight sx={{ color: '#3b82f6' }} />
          </Box>
          <Box>
            <Typography variant="h5">새 여행 만들기</Typography>
            <Typography variant="body2" color="text.secondary">여행 정보를 입력하세요</Typography>
          </Box>
        </Stack>

        {mutation.isError && <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>여행 생성에 실패했습니다.</Alert>}

        <form onSubmit={e => { e.preventDefault(); mutation.mutate() }}>
          <Stack spacing={2.5}>
            <TextField fullWidth label="여행 제목" value={title} onChange={e => setTitle(e.target.value)} required
              placeholder="예: 제주도 3박 4일" />
            <TextField fullWidth label="설명 (선택)" value={description} onChange={e => setDescription(e.target.value)}
              multiline rows={3} placeholder="여행에 대한 간단한 설명을 적어주세요" />
            <Stack direction="row" spacing={2}>
              <TextField fullWidth label="시작일" type="date" value={startDate} onChange={e => setStartDate(e.target.value)}
                required slotProps={{ inputLabel: { shrink: true } }} />
              <TextField fullWidth label="종료일" type="date" value={endDate} onChange={e => setEndDate(e.target.value)}
                required slotProps={{ inputLabel: { shrink: true } }} />
            </Stack>
            <Stack direction="row" spacing={1.5} justifyContent="flex-end">
              <Button variant="outlined" onClick={() => navigate('/trips')} sx={{ color: 'text.secondary', borderColor: '#e2e8f0' }}>
                취소
              </Button>
              <Button variant="contained" type="submit" disabled={mutation.isPending} disableElevation
                sx={{ background: 'linear-gradient(135deg, #3b82f6, #2563eb)', px: 4 }}>
                {mutation.isPending ? '생성 중...' : '여행 만들기'}
              </Button>
            </Stack>
          </Stack>
        </form>
      </Paper>
    </Box>
  )
}
