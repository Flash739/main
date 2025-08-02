import React from 'react';
import { BrowserRouter as Router, Routes, Route, useNavigate } from 'react-router-dom';
import Home from './component/Home';
import CoursePage from './component/CoursePage';
import SectionPage from './component/SectionPage';
import SubsectionPage from './component/SubsectionPage';
import LoginPage from './component/LoginPage';
import PrivateRoute from './component/PrivateRoute';
import SignupPage from './component/SignupPage';

const HomeWrapper = () => {
  const navigate = useNavigate();

  const onCourseGenerated = (taskId, title) => {
    navigate(`/course/${taskId}`, { state: { title } });
  };

  return <Home onCourseGenerated={onCourseGenerated} />;
};

const App = () => {
  return (
    <Router>
      <Routes>
        {/* Public Route */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />

        {/* Protected Routes */}
        <Route path="/" element={
          <PrivateRoute>
            <HomeWrapper />
          </PrivateRoute>
        } />
        <Route path="/course/:taskId" element={
          <PrivateRoute>
            <CoursePage />
          </PrivateRoute>
        } />
        <Route path="/course/:taskId/section/:sectionTitle" element={
          <PrivateRoute>
            <SectionPage />
          </PrivateRoute>
        } />
        <Route path="/subsection" element={
          <PrivateRoute>
            <SubsectionPage />
          </PrivateRoute>
        } />
      </Routes>
    </Router>
  );
};

export default App;
