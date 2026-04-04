import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Typography, Button, Chip, Box, Paper, Skeleton, Stack, Tabs, Tab,
  TextField, IconButton, Card, CardContent, Avatar, Dialog, DialogTitle,
  DialogContent, DialogActions, Grid, Alert, Tooltip
} from '@mui/material'
import {
  ArrowBack, CalendarMonth, People, Place, AccountBalanceWallet,
  Add, Delete, ContentCopy, Schedule, Category, CheckCircle, Cancel, HourglassEmpty
} from '@mui/icons-material'
import { tripApi } from '../../api/trips'
import { useAppSelector } from '../../store/hooks'
import type {
  TripMemberResponse, TripScheduleResponse, TripPlaceResponse,
  TripExpenseResponse, TripJoinRequestResponse
} from '../../types'

const statusConfig: Record<string, { label: string; color: 'info' | 'warning' | 'success' | 'error' }> = {
  PLANNED: { label: '예정', color: 'info' },
  IN_PROGRESS: { label: '진행 중', color: 'warning' },
  COMPLETED: { label: '완료', color: 'success' },
  CANCELLED: { label: '취소됨', color: 'error' },
}

export default function TripDetailPage() {
  const { id } = useParams<{ id: string }>()
  const tripId = Number(id)
  const navigate = useNavigate()
  const { user } = useAppSelector(s => s.auth)
  const [tab, setTab] = useState(0)

  const { data: trip, isLoading } = useQuery({
    queryKey: ['trip', id],
    queryFn: () => tripApi.getById(tripId).then(r => r.data.data),
    enabled: !!id,
  })

  if (isLoading) return <Skeleton height={400} variant="rounded" sx={{ borderRadius: 4 }} />
  if (!trip) return <Typography color="text.secondary">여행을 찾을 수 없습니다.</Typography>

  const cfg = statusConfig[trip.status] || statusConfig.PLANNED

  return (
    <Box>
      <Button startIcon={<ArrowBack />} onClick={() => navigate('/trips')} sx={{ mb: 2, color: 'text.secondary' }}>목록으로</Button>

      {/* Header */}
      <Paper elevation={0} sx={{ border: '1px solid #e2e8f0', overflow: 'hidden', mb: 3 }}>
        <Box sx={{ height: 8, background: 'linear-gradient(90deg, #3b82f6, #06b6d4)' }} />
        <Box sx={{ p: 3 }}>
          <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
            <Box>
              <Stack direction="row" spacing={1.5} alignItems="center">
                <Typography variant="h4">{trip.title}</Typography>
                <Chip label={cfg.label} color={cfg.color} />
              </Stack>
              {trip.description && <Typography variant="body1" color="text.secondary" sx={{ mt: 1 }}>{trip.description}</Typography>}
              <Stack direction="row" spacing={2} sx={{ mt: 1.5, color: 'text.secondary' }}>
                <Stack direction="row" spacing={0.5} alignItems="center">
                  <CalendarMonth sx={{ fontSize: 16 }} />
                  <Typography variant="body2">{trip.startDate} ~ {trip.endDate}</Typography>
                </Stack>
              </Stack>
            </Box>
          </Stack>
        </Box>
      </Paper>

      {/* Tabs */}
      <Paper elevation={0} sx={{ border: '1px solid #e2e8f0' }}>
        <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ px: 2, borderBottom: '1px solid #e2e8f0' }}>
          <Tab icon={<People sx={{ fontSize: 18 }} />} iconPosition="start" label="멤버" />
          <Tab icon={<Schedule sx={{ fontSize: 18 }} />} iconPosition="start" label="일정" />
          <Tab icon={<Place sx={{ fontSize: 18 }} />} iconPosition="start" label="장소" />
          <Tab icon={<AccountBalanceWallet sx={{ fontSize: 18 }} />} iconPosition="start" label="가계부" />
        </Tabs>
        <Box sx={{ p: 3 }}>
          {tab === 0 && <MembersTab tripId={tripId} />}
          {tab === 1 && <ScheduleTab tripId={tripId} startDate={trip.startDate} endDate={trip.endDate} />}
          {tab === 2 && <PlacesTab tripId={tripId} />}
          {tab === 3 && <ExpensesTab tripId={tripId} userId={user?.id || 0} />}
        </Box>
      </Paper>
    </Box>
  )
}

/* ====== Members Tab ====== */
function MembersTab({ tripId }: { tripId: number }) {
  const queryClient = useQueryClient()
  const [joinCode, setJoinCode] = useState('')
  const [copied, setCopied] = useState(false)

  const { data: members } = useQuery({
    queryKey: ['trip', tripId, 'members'],
    queryFn: () => tripApi.getMembers(tripId).then(r => r.data.data || []),
  })

  const inviteMutation = useMutation({
    mutationFn: () => tripApi.generateInviteCode(tripId),
    onSuccess: (res) => {
      const code = res.data.data?.code
      if (code) { setJoinCode(code); navigator.clipboard.writeText(code); setCopied(true); setTimeout(() => setCopied(false), 3000) }
    },
  })

  const { data: joinRequests } = useQuery({
    queryKey: ['trip', tripId, 'join-requests'],
    queryFn: () => tripApi.getJoinRequests(tripId).then(r => r.data.data || []),
  })

  const approveMutation = useMutation({
    mutationFn: (requestId: number) => tripApi.approveJoinRequest(tripId, requestId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['trip', tripId, 'members'] })
      queryClient.invalidateQueries({ queryKey: ['trip', tripId, 'join-requests'] })
    },
  })

  const rejectMutation = useMutation({
    mutationFn: (requestId: number) => tripApi.rejectJoinRequest(tripId, requestId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['trip', tripId, 'join-requests'] }),
  })

  const removeMutation = useMutation({
    mutationFn: (userId: number) => tripApi.removeMember(tripId, userId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['trip', tripId, 'members'] }),
  })

  return (
    <Box>
      {/* Pending Join Requests */}
      {joinRequests && joinRequests.length > 0 && (
        <Box sx={{ mb: 4 }}>
          <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 2 }}>
            <HourglassEmpty sx={{ fontSize: 20, color: '#f59e0b' }} />
            <Typography variant="h6">참여 요청 대기</Typography>
            <Chip label={joinRequests.length} size="small" color="warning" />
          </Stack>
          <Stack spacing={1}>
            {joinRequests.map((req: TripJoinRequestResponse) => (
              <Card key={req.id} elevation={0} sx={{ borderLeft: '3px solid #f59e0b', '&:hover': { transform: 'none' } }}>
                <CardContent sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', py: 1.5, '&:last-child': { pb: 1.5 } }}>
                  <Stack direction="row" spacing={1.5} alignItems="center">
                    <Avatar sx={{ width: 36, height: 36, bgcolor: '#fef3c7', color: '#f59e0b', fontSize: 14, fontWeight: 700 }}>
                      {req.userId}
                    </Avatar>
                    <Box>
                      <Typography fontWeight={600}>User #{req.userId}</Typography>
                      <Typography variant="caption" color="text.secondary">
                        {new Date(req.createdAt).toLocaleString()} &middot; 코드: {req.inviteCode}
                      </Typography>
                    </Box>
                  </Stack>
                  <Stack direction="row" spacing={1}>
                    <Button size="small" variant="contained" color="success" startIcon={<CheckCircle />}
                      onClick={() => approveMutation.mutate(req.id)} disabled={approveMutation.isPending}
                      disableElevation>
                      승인
                    </Button>
                    <Button size="small" variant="outlined" color="error" startIcon={<Cancel />}
                      onClick={() => rejectMutation.mutate(req.id)} disabled={rejectMutation.isPending}>
                      거절
                    </Button>
                  </Stack>
                </CardContent>
              </Card>
            ))}
          </Stack>
        </Box>
      )}

      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Typography variant="h6">팀 멤버</Typography>
        <Button variant="contained" size="small" startIcon={<Add />} onClick={() => inviteMutation.mutate()}
          disableElevation sx={{ background: 'linear-gradient(135deg, #3b82f6, #2563eb)' }}>
          초대 코드 생성
        </Button>
      </Stack>

      {joinCode && (
        <Alert severity="success" sx={{ mb: 2, borderRadius: 3 }}
          action={
            <Tooltip title={copied ? '복사됨!' : '복사'}>
              <IconButton size="small" onClick={() => { navigator.clipboard.writeText(joinCode); setCopied(true) }}>
                <ContentCopy fontSize="small" />
              </IconButton>
            </Tooltip>
          }>
          초대 코드: <strong>{joinCode}</strong> (7일간 유효)
        </Alert>
      )}

      <Stack spacing={1.5}>
        {members?.map((m: TripMemberResponse) => (
          <Card key={m.id} elevation={0} sx={{ '&:hover': { transform: 'none' } }}>
            <CardContent sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', py: 1.5, '&:last-child': { pb: 1.5 } }}>
              <Stack direction="row" spacing={1.5} alignItems="center">
                <Avatar sx={{ width: 36, height: 36, bgcolor: m.role === 'OWNER' ? 'primary.main' : '#e2e8f0', fontSize: 14 }}>
                  {m.userId}
                </Avatar>
                <Box>
                  <Typography fontWeight={600}>User #{m.userId}</Typography>
                  <Typography variant="caption" color="text.secondary">{new Date(m.joinedAt).toLocaleDateString()}</Typography>
                </Box>
              </Stack>
              <Stack direction="row" spacing={1} alignItems="center">
                <Chip label={m.role === 'OWNER' ? '방장' : '멤버'} size="small"
                  color={m.role === 'OWNER' ? 'primary' : 'default'} variant="outlined" />
                {m.role !== 'OWNER' && (
                  <IconButton size="small" onClick={() => removeMutation.mutate(m.userId)} sx={{ color: 'text.secondary', '&:hover': { color: '#ef4444' } }}>
                    <Delete fontSize="small" />
                  </IconButton>
                )}
              </Stack>
            </CardContent>
          </Card>
        ))}
      </Stack>
    </Box>
  )
}

/* ====== Schedule Tab ====== */
function ScheduleTab({ tripId, startDate, endDate }: { tripId: number; startDate: string; endDate: string }) {
  const queryClient = useQueryClient()
  const [open, setOpen] = useState(false)
  const [form, setForm] = useState({ date: startDate, title: '', memo: '', startTime: '', endTime: '' })

  const { data: schedules } = useQuery({
    queryKey: ['trip', tripId, 'schedules'],
    queryFn: () => tripApi.getSchedules(tripId).then(r => r.data.data || []),
  })

  const createMutation = useMutation({
    mutationFn: () => tripApi.createSchedule(tripId, {
      date: form.date, title: form.title, memo: form.memo || undefined,
      startTime: form.startTime || undefined, endTime: form.endTime || undefined,
    }),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['trip', tripId, 'schedules'] }); setOpen(false); setForm({ date: startDate, title: '', memo: '', startTime: '', endTime: '' }) },
  })

  const deleteMutation = useMutation({
    mutationFn: (scheduleId: number) => tripApi.deleteSchedule(tripId, scheduleId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['trip', tripId, 'schedules'] }),
  })

  const grouped = (schedules || []).reduce<Record<string, TripScheduleResponse[]>>((acc, s) => {
    (acc[s.date] = acc[s.date] || []).push(s); return acc
  }, {})

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Typography variant="h6">일정</Typography>
        <Button variant="contained" size="small" startIcon={<Add />} onClick={() => setOpen(true)}
          disableElevation sx={{ background: 'linear-gradient(135deg, #3b82f6, #2563eb)' }}>
          일정 추가
        </Button>
      </Stack>

      {!schedules?.length && (
        <Box textAlign="center" py={6} sx={{ bgcolor: '#f8fafc', borderRadius: 3, border: '2px dashed #e2e8f0' }}>
          <Schedule sx={{ fontSize: 48, color: '#cbd5e1', mb: 1 }} />
          <Typography color="text.secondary">아직 일정이 없습니다</Typography>
        </Box>
      )}

      {Object.entries(grouped).sort(([a], [b]) => a.localeCompare(b)).map(([date, items]) => (
        <Box key={date} sx={{ mb: 3 }}>
          <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1.5 }}>
            <CalendarMonth sx={{ fontSize: 18, color: 'primary.main' }} />
            <Typography fontWeight={700}>{new Date(date).toLocaleDateString('ko-KR', { month: 'long', day: 'numeric', weekday: 'short' })}</Typography>
          </Stack>
          <Stack spacing={1} sx={{ pl: 3, borderLeft: '2px solid #e2e8f0' }}>
            {items.map(s => (
              <Card key={s.id} elevation={0} sx={{ '&:hover': { transform: 'none' } }}>
                <CardContent sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', py: 1.5, '&:last-child': { pb: 1.5 } }}>
                  <Box>
                    <Typography fontWeight={600}>{s.title}</Typography>
                    <Stack direction="row" spacing={1}>
                      {s.startTime && <Typography variant="caption" color="text.secondary">{s.startTime}{s.endTime ? ` ~ ${s.endTime}` : ''}</Typography>}
                      {s.memo && <Typography variant="caption" color="text.secondary">{s.memo}</Typography>}
                    </Stack>
                  </Box>
                  <IconButton size="small" onClick={() => deleteMutation.mutate(s.id)} sx={{ color: 'text.secondary', '&:hover': { color: '#ef4444' } }}>
                    <Delete fontSize="small" />
                  </IconButton>
                </CardContent>
              </Card>
            ))}
          </Stack>
        </Box>
      ))}

      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth PaperProps={{ sx: { borderRadius: 3 } }}>
        <DialogTitle fontWeight={700}>일정 추가</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="제목" fullWidth value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} required />
            <TextField label="날짜" type="date" fullWidth value={form.date} onChange={e => setForm({ ...form, date: e.target.value })}
              slotProps={{ inputLabel: { shrink: true }, htmlInput: { min: startDate, max: endDate } }} />
            <Stack direction="row" spacing={2}>
              <TextField label="시작 시간" type="time" fullWidth value={form.startTime} onChange={e => setForm({ ...form, startTime: e.target.value })}
                slotProps={{ inputLabel: { shrink: true } }} />
              <TextField label="종료 시간" type="time" fullWidth value={form.endTime} onChange={e => setForm({ ...form, endTime: e.target.value })}
                slotProps={{ inputLabel: { shrink: true } }} />
            </Stack>
            <TextField label="메모" fullWidth multiline rows={2} value={form.memo} onChange={e => setForm({ ...form, memo: e.target.value })} />
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setOpen(false)} sx={{ color: 'text.secondary' }}>취소</Button>
          <Button variant="contained" onClick={() => createMutation.mutate()} disabled={!form.title.trim()} disableElevation>추가</Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

/* ====== Places Tab ====== */
function PlacesTab({ tripId }: { tripId: number }) {
  const queryClient = useQueryClient()
  const [open, setOpen] = useState(false)
  const [form, setForm] = useState({ name: '', address: '', category: '', notes: '' })

  const { data: places } = useQuery({
    queryKey: ['trip', tripId, 'places'],
    queryFn: () => tripApi.getPlaces(tripId).then(r => r.data.data || []),
  })

  const createMutation = useMutation({
    mutationFn: () => tripApi.createPlace(tripId, {
      name: form.name, address: form.address || undefined,
      category: form.category || undefined, notes: form.notes || undefined,
    }),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['trip', tripId, 'places'] }); setOpen(false); setForm({ name: '', address: '', category: '', notes: '' }) },
  })

  const deleteMutation = useMutation({
    mutationFn: (placeId: number) => tripApi.deletePlace(tripId, placeId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['trip', tripId, 'places'] }),
  })

  const categories = ['관광', '맛집', '카페', '숙소', '쇼핑', '기타']

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Typography variant="h6">방문 장소</Typography>
        <Button variant="contained" size="small" startIcon={<Add />} onClick={() => setOpen(true)}
          disableElevation sx={{ background: 'linear-gradient(135deg, #3b82f6, #2563eb)' }}>
          장소 추가
        </Button>
      </Stack>

      {!places?.length && (
        <Box textAlign="center" py={6} sx={{ bgcolor: '#f8fafc', borderRadius: 3, border: '2px dashed #e2e8f0' }}>
          <Place sx={{ fontSize: 48, color: '#cbd5e1', mb: 1 }} />
          <Typography color="text.secondary">아직 장소가 없습니다</Typography>
        </Box>
      )}

      <Grid container spacing={2}>
        {places?.map((p: TripPlaceResponse) => (
          <Grid size={{ xs: 12, sm: 6 }} key={p.id}>
            <Card elevation={0} sx={{ '&:hover': { transform: 'none' } }}>
              <CardContent sx={{ p: 2.5, '&:last-child': { pb: 2.5 } }}>
                <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
                  <Box>
                    <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 0.5 }}>
                      <Typography fontWeight={700}>{p.name}</Typography>
                      {p.category && <Chip label={p.category} size="small" variant="outlined" />}
                    </Stack>
                    {p.address && <Typography variant="body2" color="text.secondary">{p.address}</Typography>}
                    {p.notes && <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>{p.notes}</Typography>}
                  </Box>
                  <IconButton size="small" onClick={() => deleteMutation.mutate(p.id)} sx={{ color: 'text.secondary', '&:hover': { color: '#ef4444' } }}>
                    <Delete fontSize="small" />
                  </IconButton>
                </Stack>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth PaperProps={{ sx: { borderRadius: 3 } }}>
        <DialogTitle fontWeight={700}>장소 추가</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="장소명" fullWidth value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required />
            <TextField label="주소" fullWidth value={form.address} onChange={e => setForm({ ...form, address: e.target.value })} />
            <Stack direction="row" spacing={1} flexWrap="wrap">
              {categories.map(c => (
                <Chip key={c} label={c} onClick={() => setForm({ ...form, category: c })}
                  color={form.category === c ? 'primary' : 'default'} variant={form.category === c ? 'filled' : 'outlined'} sx={{ mb: 1 }} />
              ))}
            </Stack>
            <TextField label="메모" fullWidth multiline rows={2} value={form.notes} onChange={e => setForm({ ...form, notes: e.target.value })} />
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setOpen(false)} sx={{ color: 'text.secondary' }}>취소</Button>
          <Button variant="contained" onClick={() => createMutation.mutate()} disabled={!form.name.trim()} disableElevation>추가</Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

/* ====== Expenses Tab ====== */
function ExpensesTab({ tripId }: { tripId: number; userId: number }) {
  const queryClient = useQueryClient()
  const [open, setOpen] = useState(false)
  const [form, setForm] = useState({ category: '식비', amount: '', description: '', date: new Date().toISOString().split('T')[0] })

  const { data: expenses } = useQuery({
    queryKey: ['trip', tripId, 'expenses'],
    queryFn: () => tripApi.getExpenses(tripId).then(r => r.data.data || []),
  })

  const { data: summary } = useQuery({
    queryKey: ['trip', tripId, 'expenses', 'summary'],
    queryFn: () => tripApi.getExpenseSummary(tripId).then(r => r.data.data),
  })

  const { data: settlement } = useQuery({
    queryKey: ['trip', tripId, 'settlement'],
    queryFn: () => tripApi.getSettlement(tripId).then(r => r.data.data),
  })

  const createMutation = useMutation({
    mutationFn: () => tripApi.createExpense(tripId, {
      category: form.category, amount: Number(form.amount),
      description: form.description || undefined, date: form.date,
    }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['trip', tripId, 'expenses'] })
      setOpen(false); setForm({ category: '식비', amount: '', description: '', date: new Date().toISOString().split('T')[0] })
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (expenseId: number) => tripApi.deleteExpense(tripId, expenseId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['trip', tripId, 'expenses'] }),
  })

  const expenseCategories = ['식비', '교통', '숙박', '관광', '쇼핑', '기타']
  const categoryColors: Record<string, string> = { '식비': '#ef4444', '교통': '#3b82f6', '숙박': '#8b5cf6', '관광': '#f97316', '쇼핑': '#22c55e', '기타': '#64748b' }

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Typography variant="h6">가계부</Typography>
        <Button variant="contained" size="small" startIcon={<Add />} onClick={() => setOpen(true)}
          disableElevation sx={{ background: 'linear-gradient(135deg, #3b82f6, #2563eb)' }}>
          지출 추가
        </Button>
      </Stack>

      {/* Summary */}
      {summary && (
        <Card elevation={0} sx={{ mb: 3, background: 'linear-gradient(135deg, #f97316, #ef4444)', color: 'white', '&:hover': { transform: 'none' } }}>
          <CardContent sx={{ p: 3 }}>
            <Typography variant="body2" sx={{ opacity: 0.8 }}>총 지출</Typography>
            <Typography variant="h4" fontWeight={800}>{Number(summary.totalExpense).toLocaleString()}원</Typography>
            <Stack direction="row" spacing={1.5} sx={{ mt: 2, flexWrap: 'wrap' }}>
              {Object.entries(summary.byCategory).map(([cat, amt]) => (
                <Chip key={cat} label={`${cat} ${Number(amt).toLocaleString()}원`} size="small"
                  sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: 'white', fontWeight: 600 }} />
              ))}
            </Stack>
          </CardContent>
        </Card>
      )}

      {/* Settlement */}
      {settlement && settlement.settlements.length > 0 && (
        <Card elevation={0} sx={{ mb: 3, border: '1px solid #e2e8f0', '&:hover': { transform: 'none' } }}>
          <CardContent sx={{ p: 3 }}>
            <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 2 }}>
              <Box sx={{ width: 36, height: 36, borderRadius: '10px', bgcolor: '#eff6ff', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <AccountBalanceWallet sx={{ fontSize: 18, color: '#3b82f6' }} />
              </Box>
              <Typography variant="h6" sx={{ fontSize: '1rem' }}>정산 결과</Typography>
            </Stack>
            <Stack spacing={1}>
              {settlement.settlements.map((s, i) => (
                <Box key={i} sx={{ display: 'flex', alignItems: 'center', gap: 1.5, p: 1.5, bgcolor: '#f8fafc', borderRadius: 2 }}>
                  <Avatar sx={{ width: 28, height: 28, fontSize: 11, bgcolor: '#fef2f2', color: '#ef4444', fontWeight: 700 }}>{s.fromUserId}</Avatar>
                  <Typography variant="body2" color="text.secondary">→</Typography>
                  <Avatar sx={{ width: 28, height: 28, fontSize: 11, bgcolor: '#f0fdf4', color: '#22c55e', fontWeight: 700 }}>{s.toUserId}</Avatar>
                  <Typography variant="body2" fontWeight={700} sx={{ ml: 'auto' }}>
                    {Number(s.amount).toLocaleString()}원
                  </Typography>
                </Box>
              ))}
            </Stack>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 1.5 }}>
              ※ 토스/카카오페이에서 직접 송금해주세요
            </Typography>
          </CardContent>
        </Card>
      )}

      {!expenses?.length && !summary?.totalExpense && (
        <Box textAlign="center" py={6} sx={{ bgcolor: '#f8fafc', borderRadius: 3, border: '2px dashed #e2e8f0' }}>
          <AccountBalanceWallet sx={{ fontSize: 48, color: '#cbd5e1', mb: 1 }} />
          <Typography color="text.secondary">아직 지출이 없습니다</Typography>
        </Box>
      )}

      <Stack spacing={1}>
        {expenses?.map((e: TripExpenseResponse) => (
          <Card key={e.id} elevation={0} sx={{ '&:hover': { transform: 'none' } }}>
            <CardContent sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', py: 1.5, '&:last-child': { pb: 1.5 } }}>
              <Stack direction="row" spacing={2} alignItems="center">
                <Box sx={{
                  width: 40, height: 40, borderRadius: '10px',
                  bgcolor: `${categoryColors[e.category] || '#64748b'}15`,
                  color: categoryColors[e.category] || '#64748b',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                }}>
                  <Category sx={{ fontSize: 20 }} />
                </Box>
                <Box>
                  <Stack direction="row" spacing={1} alignItems="center">
                    <Typography fontWeight={600}>{Number(e.amount).toLocaleString()}원</Typography>
                    <Chip label={e.category} size="small" sx={{ bgcolor: `${categoryColors[e.category] || '#64748b'}15`, color: categoryColors[e.category] || '#64748b', fontWeight: 600 }} />
                  </Stack>
                  <Typography variant="caption" color="text.secondary">
                    {e.description ? `${e.description} · ` : ''}User #{e.userId} · {e.date}
                  </Typography>
                </Box>
              </Stack>
              <IconButton size="small" onClick={() => deleteMutation.mutate(e.id)} sx={{ color: 'text.secondary', '&:hover': { color: '#ef4444' } }}>
                <Delete fontSize="small" />
              </IconButton>
            </CardContent>
          </Card>
        ))}
      </Stack>

      <Dialog open={open} onClose={() => setOpen(false)} maxWidth="xs" fullWidth PaperProps={{ sx: { borderRadius: 3 } }}>
        <DialogTitle fontWeight={700}>지출 추가</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <Stack direction="row" spacing={1} flexWrap="wrap">
              {expenseCategories.map(c => (
                <Chip key={c} label={c} onClick={() => setForm({ ...form, category: c })}
                  color={form.category === c ? 'primary' : 'default'} variant={form.category === c ? 'filled' : 'outlined'} sx={{ mb: 1 }} />
              ))}
            </Stack>
            <TextField label="금액 (원)" type="number" fullWidth value={form.amount} onChange={e => setForm({ ...form, amount: e.target.value })} required />
            <TextField label="설명" fullWidth value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} />
            <TextField label="날짜" type="date" fullWidth value={form.date} onChange={e => setForm({ ...form, date: e.target.value })}
              slotProps={{ inputLabel: { shrink: true } }} />
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setOpen(false)} sx={{ color: 'text.secondary' }}>취소</Button>
          <Button variant="contained" onClick={() => createMutation.mutate()} disabled={!form.amount} disableElevation>추가</Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
