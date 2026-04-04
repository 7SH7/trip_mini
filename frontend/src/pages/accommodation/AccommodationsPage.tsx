import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Typography, TextField, Box, Grid, Card, CardContent, CardMedia, Skeleton, Stack, Chip, InputAdornment, IconButton } from '@mui/material'
import { Search, Place, Phone, Hotel } from '@mui/icons-material'
import { accommodationApi } from '../../api/accommodations'
import type { AccommodationResponse } from '../../types'

export default function AccommodationsPage() {
  const [keyword, setKeyword] = useState('')
  const [searchKeyword, setSearchKeyword] = useState('')

  const { data, isLoading } = useQuery({
    queryKey: ['accommodations', searchKeyword],
    queryFn: () => accommodationApi.search({ keyword: searchKeyword, size: 20 }).then(r => r.data.data?.content || []),
    enabled: !!searchKeyword,
  })

  const handleSearch = () => { if (keyword.trim()) setSearchKeyword(keyword.trim()) }

  return (
    <Box>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" sx={{ mb: 0.5 }}>숙소 검색</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>전국의 숙소를 검색하고 비교해보세요</Typography>
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

      {isLoading && (
        <Grid container spacing={2.5}>
          {[1,2,3,4,5,6].map(i => <Grid size={{ xs: 12, sm: 6, md: 4 }} key={i}><Skeleton height={280} variant="rounded" sx={{ borderRadius: 4 }} /></Grid>)}
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
              <CardContent sx={{ flex: 1, p: 2.5 }}>
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
                <Stack direction="row" spacing={1} alignItems="center">
                  {acc.price && <Chip label={`${acc.price.toLocaleString()}원~`} size="small" color="primary" variant="outlined" />}
                  {acc.category && <Chip label={acc.category} size="small" variant="outlined" />}
                </Stack>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  )
}
