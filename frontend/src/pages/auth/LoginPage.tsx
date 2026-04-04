import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Typography, TextField, Button, Alert, Box, Paper, Divider, Stack } from '@mui/material'
import { Google, TravelExplore } from '@mui/icons-material'
import { authApi } from '../../api/auth'
import { useAppDispatch } from '../../store/hooks'
import { login } from '../../store/authSlice'

const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID || 'your-google-client-id'
const KAKAO_CLIENT_ID = import.meta.env.VITE_KAKAO_CLIENT_ID || 'your-kakao-client-id'

export default function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const dispatch = useAppDispatch()
  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    try {
      const res = await authApi.login({ email, password })
      if (res.data.data) {
        dispatch(login(res.data.data))
        navigate('/')
      }
    } catch {
      setError('이메일 또는 비밀번호가 올바르지 않습니다.')
    }
  }

  const handleGoogleLogin = () => {
    localStorage.setItem('oauth_provider', 'google')
    const redirectUri = `${window.location.origin}/oauth/callback`
    window.location.href = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${GOOGLE_CLIENT_ID}&redirect_uri=${redirectUri}&response_type=code&scope=email%20profile&prompt=consent`
  }

  const handleKakaoLogin = () => {
    localStorage.setItem('oauth_provider', 'kakao')
    const redirectUri = `${window.location.origin}/oauth/callback`
    window.location.href = `https://kauth.kakao.com/oauth/authorize?client_id=${KAKAO_CLIENT_ID}&redirect_uri=${redirectUri}&response_type=code`
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
          <Typography variant="h5">다시 오신 걸 환영합니다</Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>Trip 계정으로 로그인하세요</Typography>
        </Box>

        {error && <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>{error}</Alert>}

        <Stack spacing={1.5} sx={{ mb: 3 }}>
          <Button fullWidth variant="outlined" startIcon={<Google />} onClick={handleGoogleLogin}
            sx={{ py: 1.2, borderColor: '#e2e8f0', color: 'text.primary', '&:hover': { bgcolor: '#f8fafc', borderColor: '#cbd5e1' } }}>
            Google로 계속하기
          </Button>
          <Button fullWidth variant="outlined" onClick={handleKakaoLogin}
            sx={{ py: 1.2, bgcolor: '#FEE500', borderColor: '#FEE500', color: '#191919', '&:hover': { bgcolor: '#FADA0A', borderColor: '#FADA0A' } }}>
            카카오로 계속하기
          </Button>
        </Stack>

        <Divider sx={{ mb: 3, '&::before, &::after': { borderColor: '#e2e8f0' } }}>
          <Typography variant="caption" color="text.secondary">또는 이메일로 로그인</Typography>
        </Divider>

        <form onSubmit={handleSubmit}>
          <Stack spacing={2}>
            <TextField fullWidth label="이메일" type="email" value={email} onChange={e => setEmail(e.target.value)} required size="small" />
            <TextField fullWidth label="비밀번호" type="password" value={password} onChange={e => setPassword(e.target.value)} required size="small" />
            <Button fullWidth variant="contained" type="submit" size="large" disableElevation
              sx={{ background: 'linear-gradient(135deg, #3b82f6, #2563eb)' }}>
              로그인
            </Button>
          </Stack>
        </form>

        <Typography variant="body2" textAlign="center" sx={{ mt: 3, color: 'text.secondary' }}>
          계정이 없으신가요? <Link to="/register" style={{ color: '#3b82f6', fontWeight: 600 }}>회원가입</Link>
        </Typography>
      </Paper>
    </Box>
  )
}
