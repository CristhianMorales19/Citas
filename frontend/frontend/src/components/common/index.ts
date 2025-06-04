// Componentes comunes reutilizables
export { default as LoadingSpinner } from './LoadingSpinner';
export { default as ErrorMessage } from './ErrorMessage';
export { default as ActionButton } from './ActionButton';
export { default as PageContainer } from './PageContainer';
export { default as FormField } from './FormField';
export { default as FormContainer } from './FormContainer';

// Componentes de visualización de datos
export { DataTable } from './DataTable';
export { StatusChip } from './StatusChip';
export { InfoCard } from './InfoCard';
export { SearchAndFilter } from './SearchAndFilter';
export { ConfirmDialog } from './ConfirmDialog';
export { StatCard } from './StatCard';
export { NotificationSnackbar } from './NotificationSnackbar';

// Utilidades de layout
export { Spacer, FlexBox, Center, Stack, spacing } from './LayoutUtils';

// Re-exportar hooks relacionados
export { useFormState } from '../../hooks/useFormState';

// Tipos
export type { DataTableColumn, DataTableAction } from './DataTable';
export type { StatusVariant } from './StatusChip';
export type { FilterField, FilterOption } from './SearchAndFilter';
