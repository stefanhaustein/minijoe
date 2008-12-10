// Simplified version of MDC clock example; original version was published
// without explicit copyright notice under the MIT license at
// http://developer.mozilla.org/en/docs/Canvas_tutorial:Basic_animations

function clock() {
  var now = new Date();
  var canvas = document.getElementById('canvas');
  var ctx = canvas.getContext('2d');

  var w = canvas.width;
  var h = canvas.height;

  ctx.save();
  ctx.translate(w / 2, h / 2);

  var scale = Math.min(w, h) / 400;

  ctx.scale(scale, scale);

  ctx.beginPath();
  ctx.fillStyle = '#325fA2';
  ctx.arc(0, 0, 142 + 6, 0, Math.PI * 2, true);
  ctx.fill();

  ctx.beginPath();
  ctx.fillStyle = '#ffffff';
  ctx.arc(0, 0, 142 - 6, 0, Math.PI * 2, true);
  ctx.fill();

  ctx.rotate(-Math.PI / 2);
  ctx.strokeStyle = "black";
  ctx.fillStyle = "black";
  ctx.lineWidth = 8;
  ctx.lineCap = "round";

  // Hour marks
  for (i = 0; i < 12; i++) {
    ctx.save();
    ctx.rotate(i * Math.PI / 6);
    ctx.fillRect(110, -2, 10, 4);
    ctx.restore();
  }

  // Minute marks
  ctx.lineWidth = 5;
  for (i = 0; i < 60; i++) {
     if (i % 5 != 0) {
       ctx.save();
       ctx.rotate(i * Math.PI / 30);
       ctx.beginPath();
       ctx.moveTo(117, 0);
       ctx.lineTo(120, 0);
       ctx.stroke();
       ctx.restore();
     }
   }

  var sec = now.getSeconds();
  var min = now.getMinutes();
  var hr = now.getHours();
  hr = hr % 12;

  // write Hours
  ctx.save();
  ctx.rotate(hr * (Math.PI / 6) + (Math.PI / 360) * min +
      (Math.PI / 21600) * sec)
  ctx.fillRect(-5, -5, 85, 10);
  ctx.restore();

  // write Minutes
  ctx.save();
  ctx.rotate((Math.PI / 30) * min + (Math.PI / 1800) * sec);
  ctx.fillRect(-3, -3, 112 + 6, 6);
  ctx.restore();

  // Write seconds
  ctx.save();
  ctx.rotate(sec * Math.PI / 30);
  ctx.strokeStyle = "#D40000";
  ctx.fillStyle = "#D40000";
  ctx.lineWidth = 6;
  ctx.beginPath();
  ctx.moveTo(-30, 0);
  ctx.lineTo(83, 0);
  ctx.stroke();

  ctx.beginPath();
  ctx.arc(0, 0, 10, 0, Math.PI * 2, true);
  ctx.fill();

  // outer circle
  ctx.beginPath();
  ctx.arc(95, 0, 10, 0, Math.PI * 2, true);
  ctx.stroke();

  ctx.fillStyle = "#555";
  ctx.beginPath();
  ctx.arc(0, 0, 3, 0, Math.PI * 2, true);
  ctx.fill();
  ctx.restore();

  ctx.restore();
}

clock();
setInterval(clock, 1000);
