import React, { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import axios from 'axios';
import QuizComponent from './QuizComponent';
import { useDispatch, useSelector } from 'react-redux';
import instance from '../component/AxiosInstance';
import { setSubsection } from '../redux/courseSlice';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeRaw from 'rehype-raw';

const SubsectionPage = () => {
  const { state } = useLocation();
  const { taskId, sectionTitle, subsectionTitle } = state || {};

  const dispatch = useDispatch();
  const key = `${taskId}_${sectionTitle}_${subsectionTitle}`;
  const cachedSubsection = useSelector((state) => state.course.subsections[key]);

  const [subsection, setSubsectionLocal] = useState(cachedSubsection || null);
  const [loading, setLoading] = useState(!cachedSubsection);

  useEffect(() => {
    if (!taskId || !sectionTitle || !subsectionTitle) return;
    if (cachedSubsection) return;

    const fetchSubsection = async () => {
      try {
        await instance.post('http://localhost:8080/api/course/highpriority', {
          taskId,
          sectionTitle,
          subsectionTitle,
        });

        const interval = setInterval(async () => {
          try {
            const res = await instance.post('http://localhost:8080/api/course/resultpriority', {
              taskId,
              sectionTitle,
              subsectionTitle,
            });

            if (res.data?.subsection?.title) {
              setSubsectionLocal(res.data.subsection);
              dispatch(setSubsection({ key, data: res.data.subsection }));
              setLoading(false);
              clearInterval(interval);
            }
          } catch (err) {
            console.error('Polling error:', err);
          }
        }, 2000);

        return () => clearInterval(interval);
      } catch (err) {
        console.error('Fetch initiation error:', err);
        setLoading(false);
      }
    };

    fetchSubsection();
  }, [taskId, sectionTitle, subsectionTitle, cachedSubsection, dispatch, key]);

  if (loading) return <p style={{ padding: '2rem' }}>⏳ Loading subsection...</p>;
  if (!subsection) return <p style={{ padding: '2rem' }}>❌ Subsection not found.</p>;

  return (
    <div style={{ padding: '2rem', background: '#fff', minHeight: '100vh' }}>
      <h1 style={{ fontSize: '1.8rem', fontWeight: 'bold', marginBottom: '1rem' }}>
        📘 {subsection.title}
      </h1>

      {/* Use ReactMarkdown for rich description rendering */}
      <ReactMarkdown
        children={subsection.description}
        remarkPlugins={[remarkGfm]}
        rehypePlugins={[rehypeRaw]}
        components={{
          a: ({ node, ...props }) => (
            <a {...props} target="_blank" rel="noopener noreferrer" />
          ),
        }}
      />

      {subsection.videos?.map((video, idx) => (
        <div key={idx} style={{ margin: '1.5rem 0' }}>
          <h4>{video.title}</h4>
          {video.url.includes("youtube.com") ? (
            <iframe
              width="560"
              height="315"
              src={video.url.replace("watch?v=", "embed/")}
              title={video.title}
              frameBorder="0"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              allowFullScreen
            ></iframe>
          ) : (
            <a href={video.url} target="_blank" rel="noopener noreferrer">
              ▶️ Watch Video
            </a>
          )}
        </div>
      ))}

      {subsection.quiz ? (
        <QuizComponent subsection={subsection} />
      ) : (
        <p>ℹ️ No quiz for this subsection.</p>
      )}
    </div>
  );
};

export default SubsectionPage;
