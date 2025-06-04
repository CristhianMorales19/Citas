import React from 'react';
import {
  Box,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
  Grid,
  Paper,
  Collapse,
  Typography,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import FilterListIcon from '@mui/icons-material/FilterList';
import ClearIcon from '@mui/icons-material/Clear';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';

export interface FilterOption {
  value: string | number;
  label: string;
}

export interface FilterField {
  name: string;
  label: string;
  type: 'select' | 'text' | 'date' | 'number';
  options?: FilterOption[];
  placeholder?: string;
  fullWidth?: boolean;
}

interface SearchAndFilterProps {
  searchValue?: string;
  onSearchChange?: (value: string) => void;
  searchPlaceholder?: string;
  filters?: FilterField[];
  filterValues?: Record<string, any>;
  onFilterChange?: (name: string, value: any) => void;
  onClearFilters?: () => void;
  showAdvancedFilters?: boolean;
  onToggleAdvancedFilters?: () => void;
  advancedFiltersExpanded?: boolean;
  elevation?: number;
  spacing?: number;
  hideSearch?: boolean;
  hideFilters?: boolean;
}

export const SearchAndFilter: React.FC<SearchAndFilterProps> = ({
  searchValue = '',
  onSearchChange,
  searchPlaceholder = 'Buscar...',
  filters = [],
  filterValues = {},
  onFilterChange,
  onClearFilters,
  showAdvancedFilters = false,
  onToggleAdvancedFilters,
  advancedFiltersExpanded = false,
  elevation = 1,
  spacing = 2,
  hideSearch = false,
  hideFilters = false,
}) => {
  const hasActiveFilters = Object.values(filterValues).some(value => 
    value !== '' && value !== null && value !== undefined
  );

  const basicFilters = filters.filter(filter => !filter.fullWidth);
  const advancedFilters = filters.filter(filter => filter.fullWidth);

  const renderFilterField = (filter: FilterField) => {
    const value = filterValues[filter.name] || '';

    switch (filter.type) {
      case 'select':
        return (
          <FormControl fullWidth size="small">
            <InputLabel>{filter.label}</InputLabel>
            <Select
              value={value}
              label={filter.label}
              onChange={(e) => onFilterChange?.(filter.name, e.target.value)}
            >
              <MenuItem value="">
                <em>Todos</em>
              </MenuItem>
              {filter.options?.map((option) => (
                <MenuItem key={option.value} value={option.value}>
                  {option.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        );

      case 'date':
        return (
          <TextField
            fullWidth
            size="small"
            type="date"
            label={filter.label}
            value={value}
            onChange={(e) => onFilterChange?.(filter.name, e.target.value)}
            InputLabelProps={{ shrink: true }}
          />
        );

      case 'number':
        return (
          <TextField
            fullWidth
            size="small"
            type="number"
            label={filter.label}
            value={value}
            onChange={(e) => onFilterChange?.(filter.name, e.target.value)}
            placeholder={filter.placeholder}
          />
        );

      default:
        return (
          <TextField
            fullWidth
            size="small"
            label={filter.label}
            value={value}
            onChange={(e) => onFilterChange?.(filter.name, e.target.value)}
            placeholder={filter.placeholder}
          />
        );
    }
  };

  if (hideSearch && hideFilters) {
    return null;
  }

  return (
    <Paper elevation={elevation} sx={{ p: 2, mb: 2 }}>
      <Grid container spacing={spacing} alignItems="center">
        {/* Search Field */}
        {!hideSearch && (
          <Grid item xs={12} md={showAdvancedFilters ? 6 : 4}>
            <TextField
              fullWidth
              size="small"
              placeholder={searchPlaceholder}
              value={searchValue}
              onChange={(e) => onSearchChange?.(e.target.value)}
              InputProps={{
                startAdornment: <SearchIcon sx={{ mr: 1, color: 'action.active' }} />,
              }}
            />
          </Grid>
        )}

        {/* Basic Filters */}
        {!hideFilters && basicFilters.map((filter) => (
          <Grid item xs={12} sm={6} md={3} key={filter.name}>
            {renderFilterField(filter)}
          </Grid>
        ))}

        {/* Filter Actions */}
        {!hideFilters && (
          <Grid item xs={12} md="auto">
            <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
              {hasActiveFilters && (
                <Button
                  size="small"
                  startIcon={<ClearIcon />}
                  onClick={onClearFilters}
                >
                  Limpiar
                </Button>
              )}
              
              {showAdvancedFilters && advancedFilters.length > 0 && (
                <Button
                  size="small"
                  startIcon={<FilterListIcon />}
                  endIcon={advancedFiltersExpanded ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                  onClick={onToggleAdvancedFilters}
                >
                  Filtros Avanzados
                </Button>
              )}
            </Box>
          </Grid>
        )}
      </Grid>

      {/* Advanced Filters */}
      {showAdvancedFilters && advancedFilters.length > 0 && (
        <Collapse in={advancedFiltersExpanded}>
          <Box sx={{ mt: 2, pt: 2, borderTop: 1, borderColor: 'divider' }}>
            <Typography variant="subtitle2" gutterBottom>
              Filtros Avanzados
            </Typography>
            <Grid container spacing={spacing}>
              {advancedFilters.map((filter) => (
                <Grid item xs={12} sm={6} md={4} key={filter.name}>
                  {renderFilterField(filter)}
                </Grid>
              ))}
            </Grid>
          </Box>
        </Collapse>
      )}
    </Paper>
  );
};
