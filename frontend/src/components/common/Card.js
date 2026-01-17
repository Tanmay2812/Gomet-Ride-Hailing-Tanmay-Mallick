import React from 'react';
import './Card.css';

const Card = ({ 
  children, 
  className = '', 
  hover = false, 
  padding = '1.5rem',
  onClick,
  style = {}
}) => {
  return (
    <div 
      className={`card ${hover ? 'card-hover' : ''} ${className}`}
      style={{ padding, ...style }}
      onClick={onClick}
    >
      {children}
    </div>
  );
};

export default Card;
