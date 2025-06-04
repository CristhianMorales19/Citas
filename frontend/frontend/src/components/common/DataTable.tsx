import React from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Typography,
  Box,
  TablePagination,
  IconButton,
  Tooltip,
} from '@mui/material';
import ErrorMessage from './ErrorMessage';
import LoadingSpinner from './LoadingSpinner';

export interface DataTableColumn<T = any> {
  id: string;
  label: string;
  minWidth?: number;
  align?: 'left' | 'right' | 'center';
  format?: (value: any, row: T) => React.ReactNode;
  sortable?: boolean;
}

export interface DataTableAction<T = any> {
  label: string;
  icon: React.ReactNode;
  onClick: (row: T) => void;
  disabled?: (row: T) => boolean;
  color?: 'primary' | 'secondary' | 'error' | 'warning' | 'info' | 'success';
}

interface DataTableProps<T = any> {
  columns: DataTableColumn<T>[];
  data: T[];
  loading?: boolean;
  error?: string | null;
  title?: string;
  actions?: DataTableAction<T>[];
  page?: number;
  rowsPerPage?: number;
  totalCount?: number;
  onPageChange?: (event: unknown, newPage: number) => void;
  onRowsPerPageChange?: (event: React.ChangeEvent<HTMLInputElement>) => void;
  emptyMessage?: string;
  maxHeight?: number | string;
  stickyHeader?: boolean;
  dense?: boolean;
  onRowClick?: (row: T) => void;
  getRowId?: (row: T, index: number) => string | number;
}

export const DataTable = <T extends Record<string, any>>({
  columns,
  data,
  loading = false,
  error = null,
  title,
  actions = [],
  page = 0,
  rowsPerPage = 10,
  totalCount,
  onPageChange,
  onRowsPerPageChange,
  emptyMessage = 'No hay datos disponibles',
  maxHeight = 400,
  stickyHeader = true,
  dense = false,
  onRowClick,
  getRowId = (row, index) => row.id || index,
}: DataTableProps<T>) => {
  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight={200}>
        <LoadingSpinner size={48} />
      </Box>
    );
  }

  if (error) {
    return <ErrorMessage message={error} variant="outlined" />;
  }

  const hasActions = actions.length > 0;
  const showPagination = onPageChange && onRowsPerPageChange;
  const count = totalCount !== undefined ? totalCount : data.length;

  return (
    <Paper elevation={1}>
      {title && (
        <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
          <Typography variant="h6" component="h2">
            {title}
          </Typography>
        </Box>
      )}

      <TableContainer sx={{ maxHeight }}>
        <Table stickyHeader={stickyHeader} size={dense ? 'small' : 'medium'}>
          <TableHead>
            <TableRow>
              {columns.map((column) => (
                <TableCell
                  key={column.id}
                  align={column.align || 'left'}
                  style={{ minWidth: column.minWidth }}
                  sx={{
                    fontWeight: 'bold',
                    backgroundColor: 'grey.50',
                  }}
                >
                  {column.label}
                </TableCell>
              ))}
              {hasActions && (
                <TableCell
                  align="center"
                  sx={{
                    fontWeight: 'bold',
                    backgroundColor: 'grey.50',
                    minWidth: 120,
                  }}
                >
                  Acciones
                </TableCell>
              )}
            </TableRow>
          </TableHead>
          <TableBody>
            {data.length === 0 ? (
              <TableRow>
                <TableCell
                  colSpan={columns.length + (hasActions ? 1 : 0)}
                  align="center"
                  sx={{ py: 4 }}
                >
                  <Typography variant="body2" color="text.secondary">
                    {emptyMessage}
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              data.map((row, index) => {
                const rowId = getRowId(row, index);
                return (
                  <TableRow
                    hover
                    key={rowId}
                    onClick={onRowClick ? () => onRowClick(row) : undefined}
                    sx={{
                      cursor: onRowClick ? 'pointer' : 'default',
                      '&:hover': onRowClick ? { backgroundColor: 'action.hover' } : undefined,
                    }}
                  >
                    {columns.map((column) => {
                      const value = row[column.id];
                      const formattedValue = column.format ? column.format(value, row) : value;
                      
                      return (
                        <TableCell key={column.id} align={column.align || 'left'}>
                          {formattedValue}
                        </TableCell>
                      );
                    })}
                    {hasActions && (
                      <TableCell align="center">
                        <Box sx={{ display: 'flex', gap: 0.5, justifyContent: 'center' }}>
                          {actions.map((action, actionIndex) => {
                            const isDisabled = action.disabled ? action.disabled(row) : false;
                            
                            return (
                              <Tooltip key={actionIndex} title={action.label}>
                                <span>
                                  <IconButton
                                    size="small"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      action.onClick(row);
                                    }}
                                    disabled={isDisabled}
                                    color={action.color || 'primary'}
                                  >
                                    {action.icon}
                                  </IconButton>
                                </span>
                              </Tooltip>
                            );
                          })}
                        </Box>
                      </TableCell>
                    )}
                  </TableRow>
                );
              })
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {showPagination && (
        <TablePagination
          rowsPerPageOptions={[5, 10, 25, 50]}
          component="div"
          count={count}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={onPageChange}
          onRowsPerPageChange={onRowsPerPageChange}
          labelRowsPerPage="Filas por página:"
          labelDisplayedRows={({ from, to, count }) =>
            `${from}-${to} de ${count !== -1 ? count : `más de ${to}`}`
          }
        />
      )}
    </Paper>
  );
};
