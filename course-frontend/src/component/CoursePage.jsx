import React, { useEffect, useState } from 'react';
import { useParams, useLocation, Link } from 'react-router-dom';
import axios from 'axios';
import { useDispatch, useSelector } from 'react-redux';
import { setOutline } from '../redux/courseSlice';
import instance from '../component/AxiosInstance';

const CoursePage = () => {
  const { taskId } = useParams();
  const { state } = useLocation(); // contains course title
  const dispatch = useDispatch();

  // Redux selector for cached course outlines
  const cachedCourse = useSelector((store) => store.course.outlines[taskId]);
  const [course, setCourse] = useState(cachedCourse || null);

  useEffect(() => {
    // If course already in Redux, use it
    if (cachedCourse) return;

    // Else poll until course is ready
    const interval = setInterval(async () => {
      try {
        const res = await instance.get(`api/course/polling/${taskId}`);
        if (res.data?.sections?.length) {
          setCourse(res.data);
          dispatch(setOutline({ taskId, outline: res.data }));
          clearInterval(interval);
        }
      } catch (e) {
        console.error('Polling failed', e);
        clearInterval(interval);
      }
    }, 2000);

    return () => clearInterval(interval);
  }, [taskId, cachedCourse, dispatch]);

  return (
    <div style={{ padding: '2rem', background: '#f9fafb', minHeight: '100vh' }}>
      <h1 style={{ fontSize: '2rem', fontWeight: 'bold', textAlign: 'center', marginBottom: '2rem' }}>
        📘 {state?.title || course?.topic} Course
      </h1>

      {!course && (
        <p style={{ textAlign: 'center', fontSize: '1.2rem' }}>⏳ Loading course content...</p>
      )}

      {course && course.sections.map((section, idx) => (
        <Link
          to={`/course/${taskId}/section/${encodeURIComponent(section.title)}`}
          state={{ taskId, sectionTitle: section.title }}
          key={idx}
          style={{
            display: 'block',
            margin: '1rem auto',
            maxWidth: '600px',
            backgroundColor: 'white',
            padding: '1.5rem',
            borderRadius: '0.75rem',
            boxShadow: '0 4px 8px rgba(0,0,0,0.1)',
            textDecoration: 'none',
            color: '#111',
            transition: 'transform 0.2s ease',
          }}
          onMouseOver={e => e.currentTarget.style.transform = 'scale(1.02)'}
          onMouseOut={e => e.currentTarget.style.transform = 'scale(1)'}
        >
          <h2 style={{ fontSize: '1.25rem', fontWeight: '600' }}>{section.title}</h2>
        </Link>
      ))}
    </div>
  );
};

export default CoursePage;
