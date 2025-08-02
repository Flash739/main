import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import axios from 'axios';
import instance from '../component/AxiosInstance';
import { setSection } from '../redux/courseSlice';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeRaw from 'rehype-raw';


const SectionPage = () => {
  const { state } = useLocation();
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const { taskId, sectionTitle } = state || {};
  const key = `${taskId}_${sectionTitle}`;
  const reduxSection = useSelector((state) => state.course.sections[key]);

  const [section, setSectionLocal] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!taskId || !sectionTitle) return;

    // Use cached version if available
    if (reduxSection) {
      setSectionLocal(reduxSection);
      setLoading(false);
      return;
    }

    const fetchSection = async () => {
      try {
        await instance.post('api/course/highpriority', {
          taskId,
          sectionTitle,
        });

        const interval = setInterval(async () => {
          try {
            const res = await instance.post('api/course/resultpriority', {
              taskId,
              sectionTitle,
              subsectionTitle: null,
            });

            if (res.data?.section?.title) {
              setSectionLocal(res.data.section);
              dispatch(setSection({ key, data: res.data.section }));
              setLoading(false);
              clearInterval(interval);
            }
          } catch (err) {
            console.error('Polling failed:', err);
            clearInterval(interval);
            setLoading(false);
          }
        }, 2000);

        return () => clearInterval(interval);
      } catch (err) {
        console.error('High priority request failed:', err);
        setLoading(false);
      }
    };

    fetchSection();
  }, [taskId, sectionTitle, reduxSection, dispatch]);

  if (loading || !section) {
    return <div style={{ padding: '2rem' }}>⏳ Loading section details...</div>;
  }

  return (
    <div style={{ padding: '2rem', backgroundColor: '#f9f9f9', minHeight: '100vh', fontFamily: 'Segoe UI, sans-serif' }}>
      <h1 style={{ fontSize: '2.2rem', fontWeight: '700', marginBottom: '0.5rem', color: '#1a202c' }}>
        📗 {section.title}
      </h1>
{section.description
  .split(/\n{2,}/) // Split into paragraphs
  .map((para, idx) => {
    const parts = para.split(/Learn more:/);

    if (parts.length === 2) {
      const [text, url] = parts;
      return (
        <div key={idx} style={{ marginBottom: '1rem' }}>
          <ReactMarkdown
            remarkPlugins={[remarkGfm]}
            rehypePlugins={[rehypeRaw]}
          >
            {text.substring(0,text.length-1).trim()}
          </ReactMarkdown>
          <a
            href={url.trim()}
            target="_blank"
            rel="noopener noreferrer"
            style={{ color: '#1a73e8', textDecoration: 'underline' }}
          >
            Learn more: {url.trim()}
          </a>
        </div>
      );
    }

    return (
      <div key={idx} style={{ marginBottom: '1rem' }}>
        <ReactMarkdown
          remarkPlugins={[remarkGfm]}
          rehypePlugins={[rehypeRaw]}
        >
          {para.trim()}
        </ReactMarkdown>
      </div>
    );
  })}


     {section.videos?.map((video, idx) => (
        <div key={idx} style={{ margin: '1rem 0' }}>
          <h4>{video.title}</h4>

          {/* For YouTube URLs — embed if possible */}
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
            // For other video URLs, just link
            <a href={video.url} target="_blank" rel="noopener noreferrer">
              Watch Video
            </a>
          )}
        </div>
      ))}


      <h2 style={{ fontSize: '1.3rem', fontWeight: 'bold', marginBottom: '1rem' }}>
        📚 Subsections
      </h2>
      {section.subsections?.length > 0 ? (
        section.subsections.map((sub, idx) => (
          <div
            key={idx}
            onClick={() =>
              navigate('/subsection', {
                state: {
                  taskId,
                  sectionTitle,
                  subsectionTitle: sub.title,
                },
              })
            }
            style={{
              cursor: 'pointer',
              padding: '1rem',
              marginBottom: '1rem',
              border: '1px solid #e2e8f0',
              borderRadius: '0.5rem',
              background: '#f1f5f9',
            }}
          >
            <h3 style={{ margin: 0, fontSize: '1.1rem' }}>📘 {sub.title}</h3>
          </div>
        ))
      ) : (
        <p>ℹ️ No subsections available.</p>
      )}
    </div>
  );
};

export default SectionPage;
