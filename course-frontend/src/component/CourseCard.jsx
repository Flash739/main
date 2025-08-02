import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../Styles/styles.css';

const CourseCard = ({ title, taskId }) => {
  const navigate = useNavigate();

  return (
    <div className="card" onClick={() => navigate(`/course/${taskId}`, { state: { title } })}>
      <h2>{title}</h2>
      <p>Click to explore course</p>
    </div>
  );
};

export default CourseCard;
