import React from 'react';
import { Box, BoxProps } from '@mui/material';

// Spacing utilities based on Material-UI theme spacing
export const spacing = {
  xs: 0.5,  // 4px
  sm: 1,    // 8px
  md: 2,    // 16px
  lg: 3,    // 24px
  xl: 4,    // 32px
  xxl: 6,   // 48px
};

interface SpacerProps {
  size?: keyof typeof spacing | number;
  direction?: 'horizontal' | 'vertical' | 'both';
}

export const Spacer: React.FC<SpacerProps> = ({
  size = 'md',
  direction = 'vertical',
}) => {
  const spaceValue = typeof size === 'number' ? size : spacing[size];

  const getSpacing = () => {
    switch (direction) {
      case 'horizontal':
        return { width: spaceValue * 8 };
      case 'vertical':
        return { height: spaceValue * 8 };
      case 'both':
        return { width: spaceValue * 8, height: spaceValue * 8 };
      default:
        return { height: spaceValue * 8 };
    }
  };

  return <Box sx={getSpacing()} />;
};

interface FlexBoxProps extends BoxProps {
  direction?: 'row' | 'column' | 'row-reverse' | 'column-reverse';
  align?: 'flex-start' | 'center' | 'flex-end' | 'stretch' | 'baseline';
  justify?: 'flex-start' | 'center' | 'flex-end' | 'space-between' | 'space-around' | 'space-evenly';
  wrap?: 'nowrap' | 'wrap' | 'wrap-reverse';
  gap?: keyof typeof spacing | number;
  inline?: boolean;
}

export const FlexBox: React.FC<FlexBoxProps> = ({
  children,
  direction = 'row',
  align = 'stretch',
  justify = 'flex-start',
  wrap = 'nowrap',
  gap = 'md',
  inline = false,
  sx,
  ...props
}) => {
  const gapValue = typeof gap === 'number' ? gap : spacing[gap];

  return (
    <Box
      sx={{
        display: inline ? 'inline-flex' : 'flex',
        flexDirection: direction,
        alignItems: align,
        justifyContent: justify,
        flexWrap: wrap,
        gap: gapValue,
        ...sx,
      }}
      {...props}
    >
      {children}
    </Box>
  );
};

interface CenterProps extends BoxProps {
  height?: string | number;
  minHeight?: string | number;
}

export const Center: React.FC<CenterProps> = ({
  children,
  height,
  minHeight = '200px',
  sx,
  ...props
}) => {
  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        height,
        minHeight,
        ...sx,
      }}
      {...props}
    >
      {children}
    </Box>
  );
};

interface StackProps extends BoxProps {
  spacing?: keyof typeof spacing | number;
  divider?: React.ReactElement;
}

export const Stack: React.FC<StackProps> = ({
  children,
  spacing: spacingProp = 'md',
  divider,
  sx,
  ...props
}) => {
  const spaceValue = typeof spacingProp === 'number' ? spacingProp : spacing[spacingProp];
  
  const childrenArray = React.Children.toArray(children);
  
  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        ...sx,
      }}
      {...props}
    >
      {childrenArray.map((child, index) => (
        <React.Fragment key={index}>
          {child}
          {index < childrenArray.length - 1 && (
            <>
              {divider && React.cloneElement(divider)}
              <Spacer size={spaceValue} />
            </>
          )}
        </React.Fragment>
      ))}
    </Box>
  );
};
