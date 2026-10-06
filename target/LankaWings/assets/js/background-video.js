document.addEventListener('DOMContentLoaded', () => {
  const video = document.querySelector('.page-background-video');
  const toggle = document.querySelector('.video-toggle');
  if (!video || !toggle) return;
  const motion = window.matchMedia('(prefers-reduced-motion: reduce)');
  let userPaused = false;
  video.muted = true;
  const label = () => {
    toggle.textContent = video.paused ? 'Play video' : 'Pause video';
    toggle.setAttribute('aria-label', video.paused ? 'Play background video' : 'Pause background video');
  };
  const play = () => {
    if (motion.matches || document.hidden || userPaused) return;
    video.play().then(() => {
      video.classList.add('is-playing'); toggle.hidden = motion.matches; label();
    }).catch(() => { toggle.hidden = motion.matches; label(); });
  };
  video.addEventListener('playing', () => {
    video.classList.add('is-playing'); toggle.hidden = false; label();
  });
  video.addEventListener('pause', label);
  video.addEventListener('error', () => {
    video.classList.remove('is-playing'); toggle.hidden = true;
  });
  const preferenceChanged = () => {
    if (motion.matches) {
      video.autoplay = false; video.pause(); toggle.hidden = true;
    } else { video.autoplay = true; play(); }
  };
  toggle.addEventListener('click', () => {
    userPaused = !video.paused;
    if (userPaused) video.pause(); else play();
  });
  document.addEventListener('visibilitychange', () => {
    if (document.hidden) video.pause(); else play();
  });
  motion.addEventListener('change', preferenceChanged);
  preferenceChanged();
});
