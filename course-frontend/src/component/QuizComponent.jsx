import React, { useState } from 'react';

const QuizComponent = ({ subsection }) => {
  const [selectedOptions, setSelectedOptions] = useState({}); // key = question index

  const handleSelect = (quizIndex, optionIndex) => {
    setSelectedOptions((prev) => ({
      ...prev,
      [quizIndex]: optionIndex,
    }));
  };

  return subsection.quiz && subsection.quiz.length > 0 ? (
    <div
      style={{
        padding: '1rem',
        background: '#fef3c7',
        borderRadius: '0.5rem',
        border: '1px solid #fcd34d',
      }}
    >
      <h2 style={{ fontWeight: '600', marginBottom: '0.5rem' }}>📝 Quiz</h2>

      {subsection.quiz.map((quizItem, index) => (
        <div key={index} style={{ marginBottom: '1.5rem' }}>
          <strong>Q{index + 1}. {quizItem.question}</strong>
          <ul style={{ listStyle: 'none', paddingLeft: 0 }}>
            {quizItem.options.map((option, idx) => {
              const isSelected = selectedOptions[index] !== undefined;
              const isCorrect = idx === quizItem.correctOptionIndex;
              const isClicked = selectedOptions[index] === idx;

              let bgColor = '#f3f4f6'; // default
              if (isSelected) {
                if (isClicked && isCorrect) bgColor = '#d1fae5'; // green
                else if (isClicked && !isCorrect) bgColor = '#fee2e2'; // red
                else if (isCorrect) bgColor = '#d1fae5'; // show correct
              }

              return (
                <li
                  key={idx}
                  onClick={() => !isSelected && handleSelect(index, idx)}
                  style={{
                    margin: '0.5rem 0',
                    padding: '0.5rem 0.75rem',
                    backgroundColor: bgColor,
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    cursor: isSelected ? 'default' : 'pointer',
                    transition: 'background 0.2s',
                  }}
                >
                  {isSelected && isCorrect ? '✅ ' : ''}{option}
                </li>
              );
            })}
          </ul>
        </div>
      ))}
    </div>
  ) : (
    <p>ℹ️ No quiz for this subsection.</p>
  );
};

export default QuizComponent;
