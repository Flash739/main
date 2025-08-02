// src/redux/slices/courseSlice.js
import { createSlice } from '@reduxjs/toolkit';

const initialState = {
  courses: [],         // All course cards
  outlines: {},        // taskId → full course outline
  sections: {},        // taskId_sectionTitle → section data
  subsections: {},     // taskId_sectionTitle_subsectionTitle → subsection data
};

export const courseSlice = createSlice({
  name: 'course',
  initialState,
  reducers: {
    // Set all courses
    setCourses: (state, action) => {
      state.courses = action.payload;
    },
    // Add a new course at the beginning
    addCourse: (state, action) => {
      state.courses.unshift(action.payload);
    },
    // Store course outline
    setOutline: (state, action) => {
      const { taskId, outline } = action.payload;
      state.outlines[taskId] = outline;
    },
    // Store section lazily
    setSection: (state, action) => {
      const { key, data } = action.payload;
      state.sections[key] = data;
    },
    // Store subsection lazily
    setSubsection: (state, action) => {
      const { key, data } = action.payload;
      state.subsections[key] = data;
    },
  },
});

export const {
  setCourses,
  addCourse,
  setOutline,
  setSection,
  setSubsection,
} = courseSlice.actions;

export default courseSlice.reducer;
