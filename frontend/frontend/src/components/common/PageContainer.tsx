import React from 'react';
import { Container, Paper, Typography, Box, Breadcrumbs, Link } from '@mui/material';
import { useNavigate } from 'react-router-dom';

interface BreadcrumbItem {
  label: string;
  path?: string;
}

interface PageContainerProps {
  title?: string;
  subtitle?: string;
  maxWidth?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
  children: React.ReactNode;
  paper?: boolean;
  breadcrumbs?: BreadcrumbItem[];
  headerActions?: React.ReactNode;
  sx?: object;
}

const PageContainer: React.FC<PageContainerProps> = ({
  title,
  subtitle,
  maxWidth = 'lg',
  children,
  paper = true,
  breadcrumbs = [],
  headerActions,
  sx = {}
}) => {
  const navigate = useNavigate();

  const handleBreadcrumbClick = (path: string) => {
    navigate(path);
  };

  const content = (
    <>
      {/* Breadcrumbs */}
      {breadcrumbs.length > 0 && (
        <Breadcrumbs aria-label="breadcrumb" sx={{ mb: 2 }}>
          {breadcrumbs.map((crumb, index) => {
            const isLast = index === breadcrumbs.length - 1;
            
            if (isLast || !crumb.path) {
              return (
                <Typography key={index} color="text.primary">
                  {crumb.label}
                </Typography>
              );
            }
            
            return (
              <Link
                key={index}
                color="inherit"
                href="#"
                onClick={(e) => {
                  e.preventDefault();
                  handleBreadcrumbClick(crumb.path!);
                }}
                sx={{ cursor: 'pointer' }}
              >
                {crumb.label}
              </Link>
            );
          })}
        </Breadcrumbs>
      )}

      {/* Page Header */}
      {(title || headerActions) && (
        <Box 
          display="flex" 
          justifyContent="space-between" 
          alignItems="flex-start" 
          mb={3}
        >
          <Box>
            {title && (
              <Typography variant="h4" component="h1" gutterBottom>
                {title}
              </Typography>
            )}
            {subtitle && (
              <Typography variant="subtitle1" color="text.secondary" gutterBottom>
                {subtitle}
              </Typography>
            )}
          </Box>
          {headerActions && (
            <Box display="flex" gap={1}>
              {headerActions}
            </Box>
          )}
        </Box>
      )}
      
      {children}
    </>
  );

  return (
    <Container maxWidth={maxWidth} sx={{ py: 4, ...sx }}>
      {paper ? (
        <Paper elevation={2} sx={{ p: 4 }}>
          {content}
        </Paper>
      ) : (
        <Box>{content}</Box>
      )}
    </Container>
  );
};

export default PageContainer;
