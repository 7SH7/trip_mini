import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Paper, TextField, Button, Typography, Divider, Alert, Stack, Box } from '@mui/material'
import { Google } from '@mui/icons-material'
import styled from 'styled-components'
import { useAppDispatch } from '../../store/hooks'
import { login } from '../../store/authSlice'
import { authApi } from '../../api/auth'

const AuthContainer = styled(Paper)`
  max-width: 420px;
  margin: 3rem auto;
  padding: 2.5rem;
`

const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID || 'your-google-client-id'
const KAKAO_CLIENT_ID = import.meta.env.VITE_KAKAO_CLIENT_ID || 'your-kakao-client-id'
const REDIRECT_URI = `${window.location.origin}/oauth/callback`

export default function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const dispatch = useAppDispatch()
  const navigate = useNavigate()

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError('')
    try {
      const res = await authApi.login({ email, password })
      const tokens = res.data.data!
      dispatch(login(tokens))
      navigate('/')
    } catch {
      setError('이메일 또는 비밀번호가 올바르지 않습니다.')
    }
  }

  const handleGoogleLogin = () => {
    localStorage.setItem('oauth_provider', 'google')
    const url = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${GOOGLE_CLIENT_ID}&redirect_uri=${encodeURIComponent(REDIRECT_URI)}&response_type=code&scope=email%20profile&access_type=offline`
    window.location.href = url
  }

  const handleKakaoLogin = () => {
    localStorage.setItem('oauth_provider', 'kakao')
    const url = `https://kauth.kakao.com/oauth/authorize?client_id=${KAKAO_CLIENT_ID}&redirect_uri=${encodeURIComponent(REDIRECT_URI)}&response_type=code`
    window.location.href = url
  }

  return (
    <AuthContainer elevation={0}>
      <Typography variant="h5" fontWeight={600} gutterBottom>로그인</Typography>
      <Box component="form" onSubmit={handleSubmit}>
        <Stack spacing={2}>
          <TextField label="이메일" type="email" value={email} onChange={e => setEmail(e.target.value)} required fullWidth />
          <TextField label="비밀번호" type="password" value={password} onChange={e => setPassword(e.target.value)} required fullWidth />
          {error && <Alert severity="error">{error}</Alert>}
          <Button type="submit" variant="contained" size="large" fullWidth>로그인</Button>
        </Stack>
      </Box>
      <Divider sx={{ my: 2 }}>또는</Divider>
      <Stack direction="row" spacing={1}>
        <Button variant="outlined" fullWidth startIcon={<Google />} onClick={handleGoogleLogin}>
          Google
        </Button>
        <Button variant="outlined" fullWidth onClick={handleKakaoLogin}
          sx={{ bgcolor: '#FEE500', color: '#000', borderColor: '#FEE500', '&:hover': { bgcolor: '#FDD835' } }}>
          Kakao
        </Button>
      </Stack>
      <Typography variant="body2" textAlign="center" sx={{ mt: 2 }}>
        계정이 없으신가요? <Link to="/register">회원가입</Link>
      </Typography>
    </AuthContainer>
  )
}
