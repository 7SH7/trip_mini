import { useState } from 'react'
import { useQuery, useMutation } from '@tanstack/react-query'
import {
  Typography, TextField, Box, Grid, Card, CardContent, CardMedia, Skeleton,
  Stack, Chip, InputAdornment, IconButton, Button, Tooltip, Dialog,
  DialogTitle, DialogContent, DialogActions, MenuItem, Select, FormControl, InputLabel, Alert
} from '@mui/material'
import { Search, Place, Phone, Hotel, AddLocation, HelpOutline } from '@mui/icons-material'
import { accommodationApi } from '../../api/accommodations'
import { tripApi } from '../../api/trips'
import type { AccommodationResponse, TripResponse } from '../../types'

export default function AccommodationsPage() {
  const [keyword, setKeyword] = useState('')
  const [searchKeyword, setSearchKeyword] = useState('')
  const [addDialog, setAddDialog] = useState<AccommodationResponse | null>(null)
  const [selectedTripId, setSelectedTripId] = useState<number | ''>('')
  const [success, setSuccess] = useState('')

  const { data, isLoading } = useQuery({
    queryKey: ['accommodations', searchKeyword],
    queryFn: () => accommodationApi.search({ keyword: searchKeyword, size: 20 }).then(r => r.data.data?.content || []),
    enabled: !!searchKeyword,
  })

  const { data: myTrips } = useQuery({
    queryKey: ['trips', 'my'],
    queryFn: () => tripApi.getMyTrips().then(r => r.data.data || []),
  })

  const addPlaceMutation = useMutation({
    mutationFn: ({ tripId, acc }: { tripId: number; acc: AccommodationResponse }) =>
      tripApi.createPlace(tripId, {
        name: acc.title,
        address: acc.address || undefined,
        category: '숙소',
        notes: [acc.priceRaw || (acc.price ? `${acc.price.toLocaleString()}원~` : null), acc.tel].filter(Boolean).join(' / ') || undefined,
      }),
    onSuccess: () => {
      setSuccess('여행 장소에 추가되었습니다!')
      setAddDialog(null)
      setSelectedTripId('')
      setTimeout(() => setSuccess(''), 3000)
    },
  })

  const handleSearch = () => { if (keyword.trim()) setSearchKeyword(keyword.trim()) }

  return (
    <Box>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" sx={{ mb: 0.5 }}>숙소 검색</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>전국의 숙소를 검색하고 여행 장소에 추가하세요</Typography>
        <TextField
          fullWidth placeholder="지역명 또는 숙소명을 입력하세요" value={keyword}
          onChange={e => setKeyword(e.target.value)} onKeyDown={e => e.key === 'Enter' && handleSearch()}
          slotProps={{
            input: {
              startAdornment: <InputAdornment position="start"><Search sx={{ color: 'text.secondary' }} /></InputAdornment>,
              endAdornment: (
                <InputAdornment position="end">
                  <IconButton onClick={handleSearch} edge="end" color="primary"><Search /></IconButton>
                </InputAdornment>
              ),
            }
          }}
          sx={{ maxWidth: 600, '& .MuiOutlinedInput-root': { bgcolor: 'white' } }}
        />
      </Box>

      {success && <Alert severity="success" sx={{ mb: 3, borderRadius: 3 }}>{success}</Alert>}

      {isLoading && (
        <Grid container spacing={2.5}>
          {[1,2,3,4,5,6].map(i => <Grid size={{ xs: 12, sm: 6, md: 4 }} key={i}><Skeleton height={320} variant="rounded" sx={{ borderRadius: 4 }} /></Grid>)}
        </Grid>
      )}

      {searchKeyword && !data?.length && !isLoading && (
        <Box textAlign="center" py={8} sx={{ bgcolor: '#f8fafc', borderRadius: 4, border: '2px dashed #e2e8f0' }}>
          <Hotel sx={{ fontSize: 56, color: '#cbd5e1', mb: 2 }} />
          <Typography variant="h6" color="text.secondary">검색 결과가 없습니다</Typography>
          <Typography variant="body2" color="text.secondary">다른 키워드로 검색해보세요</Typography>
        </Box>
      )}

      {!searchKeyword && !isLoading && (
        <Box textAlign="center" py={8} sx={{ bgcolor: '#f8fafc', borderRadius: 4, border: '2px dashed #e2e8f0' }}>
          <Hotel sx={{ fontSize: 56, color: '#cbd5e1', mb: 2 }} />
          <Typography variant="h6" color="text.secondary">숙소를 검색해보세요</Typography>
          <Typography variant="body2" color="text.secondary">예: 서울, 제주, 부산</Typography>
        </Box>
      )}

      <Grid container spacing={2.5}>
        {data?.map((acc: AccommodationResponse) => (
          <Grid size={{ xs: 12, sm: 6, md: 4 }} key={acc.id}>
            <Card elevation={0} sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
              {acc.imageUrl ? (
                <CardMedia component="img" height={180} image={acc.imageUrl} alt={acc.title}
                  sx={{ objectFit: 'cover' }} />
              ) : (
                <Box sx={{ height: 180, bgcolor: '#f1f5f9', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <Hotel sx={{ fontSize: 48, color: '#cbd5e1' }} />
                </Box>
              )}
              <CardContent sx={{ flex: 1, p: 2.5, display: 'flex', flexDirection: 'column' }}>
                <Typography fontWeight={700} sx={{ mb: 1, fontSize: '1.05rem' }}>{acc.title}</Typography>
                {acc.address && (
                  <Stack direction="row" spacing={0.5} alignItems="flex-start" sx={{ mb: 0.5, color: 'text.secondary' }}>
                    <Place sx={{ fontSize: 16, mt: 0.3 }} />
                    <Typography variant="body2">{acc.address}</Typography>
                  </Stack>
                )}
                {acc.tel && (
                  <Stack direction="row" spacing={0.5} alignItems="center" sx={{ mb: 1, color: 'text.secondary' }}>
                    <Phone sx={{ fontSize: 16 }} />
                    <Typography variant="body2">{acc.tel}</Typography>
                  </Stack>
                )}
                <Stack direction="row" spacing={0.5} alignItems="center" sx={{ mb: 1.5 }}>
                  {acc.price && <Chip label={`${acc.price.toLocaleString()}원~`} size="small" color="primary" variant="outlined" />}
                  {acc.price && (
                    <Tooltip title="실제 가격은 해당 숙소에서 직접 확인이 필요합니다" arrow placement="top">
                      <HelpOutline sx={{ fontSize: 16, color: '#94a3b8', cursor: 'help' }} />
                    </Tooltip>
                  )}
                  {acc.category && <Chip label={acc.category} size="small" variant="outlined" />}
                </Stack>
                <Box sx={{ mt: 'auto' }}>
                  <Button fullWidth variant="outlined" size="small" startIcon={<AddLocation />}
                    onClick={() => setAddDialog(acc)}
                    sx={{ borderColor: '#e2e8f0', color: 'text.secondary', '&:hover': { bgcolor: '#f0f7ff', borderColor: '#3b82f6', color: '#3b82f6' } }}>
                    여행 장소에 추가
                  </Button>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Add to Trip Dialog */}
      <Dialog open={!!addDialog} onClose={() => setAddDialog(null)} maxWidth="xs" fullWidth
        PaperProps={{ sx: { borderRadius: 3 } }}>
        <DialogTitle fontWeight={700}>여행 장소에 추가</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            <strong>{addDialog?.title}</strong>을(를) 어떤 여행에 추가할까요?
          </Typography>
          {addDialog?.price && (
            <Stack direction="row" spacing={0.5} alignItems="center" sx={{ mb: 2, p: 1.5, bgcolor: '#f8fafc', borderRadius: 2 }}>
              <Typography variant="body2">참고 가격: <strong>{addDialog.priceRaw || `${addDialog.price.toLocaleString()}원~`}</strong></Typography>
              <Tooltip title="실제 가격은 해당 숙소에서 직접 확인이 필요합니다" arrow>
                <HelpOutline sx={{ fontSize: 16, color: '#94a3b8', cursor: 'help' }} />
              </Tooltip>
            </Stack>
          )}
          <FormControl fullWidth size="small">
            <InputLabel>여행 선택</InputLabel>
            <Select value={selectedTripId} onChange={e => setSelectedTripId(e.target.value as number)} label="여행 선택">
              {myTrips?.map((t: TripResponse) => (
                <MenuItem key={t.id} value={t.id}>{t.title} ({t.startDate} ~ {t.endDate})</MenuItem>
              ))}
            </Select>
          </FormControl>
          {!myTrips?.length && (
            <Typography variant="body2" color="error" sx={{ mt: 1 }}>여행을 먼저 만들어주세요.</Typography>
          )}
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setAddDialog(null)} sx={{ color: 'text.secondary' }}>취소</Button>
          <Button variant="contained" disableElevation disabled={!selectedTripId || addPlaceMutation.isPending}
            onClick={() => addDialog && selectedTripId && addPlaceMutation.mutate({ tripId: selectedTripId as number, acc: addDialog })}
            sx={{ background: 'linear-gradient(135deg, #3b82f6, #2563eb)' }}>
            추가하기
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
