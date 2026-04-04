import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Typography, TextField, Button, Alert, Box, Paper, Stack } from '@mui/material'
import { TravelExplore } from '@mui/icons-material'
import { authApi } from '../../api/auth'
import { useAppDispatch } from '../../store/hooks'
import { login } from '../../store/authSlice'

export default function RegisterPage() {
  const [email, setEmail] = useState('')
  const [name, setName] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const dispatch = useAppDispatch()
  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    try {
      const res = await authApi.register({ email, name, password })
      if (res.data.data) {
        dispatch(login(res.data.data))
        navigate('/')
      }
    } catch {
      setError('회원가입에 실패했습니다. 이미 사용 중인 이메일일 수 있습니다.')
    }
  }

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '70vh' }}>
      <Paper elevation={0} sx={{ p: 5, maxWidth: 440, width: '100%', border: '1px solid #e2e8f0' }}>
        <Box textAlign="center" sx={{ mb: 4 }}>
          <Box sx={{
            width: 52, height: 52, borderRadius: '14px', mx: 'auto', mb: 2,
            background: 'linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <TravelExplore sx={{ color: 'white', fontSize: 28 }} />
          </Box>
          <Typography variant="h5">Trip에 가입하기</Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>여행의 모든 것을 경험하세요</Typography>
        </Box>

        {error && <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>{error}</Alert>}

        <form onSubmit={handleSubmit}>
          <Stack spacing={2}>
            <TextField fullWidth label="이메일" type="email" value={email} onChange={e => setEmail(e.target.value)} required size="small" />
            <TextField fullWidth label="이름" value={name} onChange={e => setName(e.target.value)} required size="small" />
            <TextField fullWidth label="비밀번호" type="password" value={password} onChange={e => setPassword(e.target.value)} required size="small"
              helperText="8자 이상 입력해주세요" />
            <Button fullWidth variant="contained" type="submit" size="large" disableElevation
              sx={{ background: 'linear-gradient(135deg, #3b82f6, #2563eb)' }}>
              가입하기
            </Button>
          </Stack>
        </form>

        <Typography variant="body2" textAlign="center" sx={{ mt: 3, color: 'text.secondary' }}>
          이미 계정이 있으신가요? <Link to="/login" style={{ color: '#3b82f6', fontWeight: 600 }}>로그인</Link>
        </Typography>
      </Paper>
    </Box>
  )
}
