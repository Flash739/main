import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import CourseCard from './CourseCard';
import axios from 'axios';
import instance from '../component/AxiosInstance';
import { useNavigate } from 'react-router-dom';
import '../Styles/home.css';
import { setCourses, addCourse } from '../redux/courseSlice';

const Home = ({ onCourseGenerated }) => {
  const [prompt, setPrompt] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const dispatch = useDispatch();
  const userId = localStorage.getItem('userId');
  const courses = useSelector((state) => state.course.courses);

  const handleGenerate = async () => {
    if (!prompt.trim()) return;
    try {
      setLoading(true);
      const res = await instance.post('api/course/generate', {
        prompt,
        userid: userId
      });

      const newCourse = {
        id: res.data.id,
        topic: res.data.topic
      };

      dispatch(addCourse(newCourse));
      setPrompt('');
    } catch (err) {
      console.error(err);
      alert('Error generating course');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token || !userId) {
      navigate('/login');
    }
  }, [userId]);

  const fetchUserCourses = async () => {
    try {
      const res = await instance.get(`api/course/${userId}/courses`);
      dispatch(setCourses(res.data));
    } catch (err) {
      if(err.response.status===403){
         localStorage.removeItem('token');
         localStorage.removeItem('userId');
         navigate('/login');
      }
      console.error('Failed to fetch user courses:', err);
    }
  };

  useEffect(() => {
    if (courses.length === 0) fetchUserCourses();
  }, [dispatch]);

  return (
    <div className="home-container">
      <h1 className="home-title">🎓 AI Course Generator</h1>

      <div className="generate-section">
        <input
          type="text"
          className="prompt-input"
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          placeholder="e.g. Deep Learning for NLP"
        />
        <button
          className={`generate-btn ${loading ? 'loading' : ''}`}
          onClick={handleGenerate}
          disabled={loading}
        >
          {loading ? 'Generating...' : 'Generate'}
        </button>
      </div>

      <h2 className="your-courses-title">📚 Your Created Courses</h2>
      <div className="course-grid">
        {courses.length > 0 ? (
          courses.map((course) => (
            <CourseCard
              key={course.id}
              title={course.topic}
              taskId={course.id}
            />
          ))
        ) : (
          <p className="no-courses-text">No courses yet. Try generating one!</p>
        )}
      </div>
    </div>
  );
};

export default Home;
