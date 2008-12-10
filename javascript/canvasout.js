// Copyright 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * @fileoverview MiniJoe Example: Simple breakout game
 * @author Stefan Haustein
 */

var w = window.innerWidth;
var h = window.innerHeight;
var size = Math.min (w, h);

var canvas = document.getElementById("canvas");

canvas.width = w;
canvas.height = h;

var level = 0;

var rows = 6;
var cols = 7;
var bricks = new Array(rows * cols);
var colors = ["#ff0000", "#ffff00", "#00ff00", "#00ffff", "#0000ff", "#ff00ff"];

var brickW = size / cols;
var brickH = size / 20;

var paddleW = brickW * 1.25;
var paddleH = brickH / 2;
var paddleX = (size - paddleW) / 2;

var lastStep = new Date();

ctx = canvas.getContext("2d");
ctx.translate((w - size) / 2, (h - size) / 2);

var ballX = size / 2;
var ballY = size - brickH * 2;
var ballSize = size / 40;

var speed = brickH * 2 / 3;
var lives = 0;
var score = 0;
var highscore = 0;
var stopPaddle = false;
var paddleDx = 0;

var dy = 0;

/**
 * Initialize the game.
 */
function init() {
  var i = 0;
  for (var r = 0; r < rows; r++) {
    for (var c = 0; c < cols; c++) {
      bricks[i++] = 1;
    }
  }
  remaining = rows * cols;
  ctx.fillStyle = "#000000";
  ctx.fillRect(-(w - size) / 2, -(h - size) / 2, w, h);

  var bw = brickW;
  var bh = brickH;

  var y = bh * 2;
  var i = 0;

  for (var r = 0; r < rows; r++) {
    var x = 0;
    ctx.fillStyle = colors[r];
    for (var c = 0; c < cols; c++) {
      if (bricks[i] > 0) {
        ctx.fillRect(x, y, bw - 1, bh - 1);
      }
      i++;
      x += bw;
    }
    if (r != 0) {
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(size, y);
      ctx.stroke();
    }
    y += bh;
  }
}

/**
 * Draw a text string at the given coordinates with the given vertical
 * ("right", "left" or "center") and horizontal ("top", "bottom") align
 */
function drawString(x, y, text, valign, halign) {
  if (ctx.mozDrawText) {
    if (valign == "top") {
      y += 20;
    }
    switch (halign) {
    case "right":
      x -= ctx.mozMeasureText(text);
      break;
    case "center":
      x -= ctx.mozMeasureText(text) / 2.0;
      break;
    }
    ctx.translate(x, y);
    ctx.mozDrawText(text);
    ctx.translate(-x, -y);
  }
}

/**
 * Perform a step.
 */
function step() {
  var time = new Date();
  var dt = time - lastStep;
  var factor = dt / 50;
  lastStep = time;

  var bw = brickW;
  var bh = brickH;
  var bs = ballSize;

  ctx.fillStyle = "#000000";
  ctx.fillRect(ballX - 1, ballY - 1, bs + 2, bs + 2);
  ctx.fillRect(paddleX - 1, size - bh - 1, paddleW + 2, bs + 2);
  ctx.fillRect(-(w - size) / 2, -(h - size) / 2, w, brickH * 2);

  paddleX += paddleDx * factor;

  if (paddleX < 0) {
    paddleX = 0;
  } else if (paddleX > size - paddleW) {
    paddleX = size - paddleW;
  }

  if (stopPaddle) {
    paddleDx = 0;
  }

  if (dy == 0) {
    ballX = paddleX + (paddleW - bs) / 2
    ballY = size - bh * 1.5;
  } else {
    ballX += dx * factor;
    ballY += dy * factor;

    if (0 >= ballX || ballX + bs >= size) {
      ballX -= dx;
      dx = -dx;
    }

    if (ballY < 0) {
      ballY -= dy;
      dy = -dy;
    } else if (ballY > size - bh) {
      if (ballX >= paddleX - ballSize && paddleX + paddleW >= ballX) {
        dx = dx + speed * 1.5 * (ballX - (paddleX + paddleW / 2)) / paddleW;
        if (dx < -speed / 2) {
          dx = -speed / 2;
        }
        if (dx > speed / 2) {
          dx = speed / 2;
        }
        dy = -speed + Math.abs(dx);
        ballX += dx;
        ballY += dy;
      } else {
        lives--;
        dy = 0;
      }
    } else {
      var row = Math.floor((ballY + bs / 2) / bh) - 2;
      var col = Math.floor((ballX + bs / 2) / bw);
      var p = row * cols + col;

      if (p >= 0 && p < bricks.length && bricks[p] > 0) {
        bricks[p] = 0;

        ctx.fillRect(col * brickW - 1, (row + 2) * brickH - 1,
            brickW + 2, brickH + 2);

        remaining -= 1;
        score += (8 - row) * 10;

        if (score > highscore) {
          highscore = score;
        }

        if (remaining == 0) {
          level += 1;
          init();
          dy = 0;
        } else{
          dy = -dy;
        }
      }
    }
  }

  ctx.fillStyle = "#ffffff";
  ctx.fillRect(ballX, ballY, bh / 2, bh / 2);
  ctx.fillRect(paddleX, size - bh, paddleW, paddleH);

  drawString(0, 0, "" + score, "top", "left");
  drawString(size, 0, "HI " + highscore, "top", "right");
  drawString(size / 2, 0, "" + lives, "top", "center");

  if (lives == 0) {
    drawString(size / 2, size / 2, "GAME OVER", "bottom", "center");
    drawString(size / 2, size / 2, "Press 'up' to start", "top", "center");
  }
  setTimeout(step, Math.max(30 - dt, 10));
}

document.onkeydown = function(e) {
  var keycode = e.keyCode;

  if (keycode == 37) { //left=move left
    paddleDx = -ballSize * 2;
    stopPaddle = false;
  } else if (keycode == 39) { //right=move right
    paddleDx = ballSize * 2;
    stopPaddle = false;
  } else if (keycode == 38 && dy == 0) { // up
    start();
  }
};

/**
 * start the game: Reset paddle, lives, redraw everything.
 */
function start() {
  dy = -speed;
  dx = 0;

  if (lives == 0) {
    score = 0;
    lives = 3;
    init();
  }
}

document.onkeyup = function(e) {
  var keycode = e.keyCode;

  if (keycode == 37 || keycode == 39) {
    stopPaddle = true;
  }
};

init();

lastStep = new Date();
setTimeout(step, 30);
