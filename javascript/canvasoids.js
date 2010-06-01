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
 * @fileoverview MiniJoe Example: Simple asteroids-style game
 * @author Stefan Haustein
 */

// Note: the html document must not have borders for the body element!

var canvas = document.getElementById("canvas");

var w = window.innerWidth;
var h = window.innerHeight;

canvas.width = w;
canvas.height = h;

// for safari
w = window.innerWidth;
h = window.innerHeight;

var base = Math.min(w, h) / 400;
var ctx = canvas.getContext("2d");

ship = new Array(6);
ship[0] = base * 10; ship[1] = 0;
ship[2] = -base * 5; ship[3] = base * 5;
ship[4] = -base * 5; ship[5] = -base * 5;

blink = 0;

big = new Array(14);
big[0] = -20 * base; big[1] = 0;
big[2] = 0; big[3] = 10 * base;
big[4] = 10 * base; big[5] = 0;
big[6] = 5 * base; big[7] = 10 * base;
big[8] = 10 * base; big[9] = 20 * base;
big[10] = 25 * base; big[11] = -10 * base;
big[12] = 0; big[13] = -20 * base;

small = new Array(8);
small[0] = -5 * base; small[1] = 7 * base;
small[2] = 6 * base; small[3] = 4 * base;
small[4] = 8 * base; small[5] = -5 * base;
small[6] = -7 * base; small[7] = -6 * base;

rocks = new Array(2);
shots = new Array(2);

score = 0;
lives = 0;
highscore = 0;
level = 0;

ship = new Sprite(ship, 0, w / 2, h / 2, 0, 0, 0, 0);
ship.resetDr = false;

/**
 * Object representing a shot
 */
function Shot() {
  this.dx = Math.cos(ship.r) * base * 15 + ship.dx;
  this.dy = Math.sin(ship.r) * base * 15 + ship.dy;
  this.x = ship.x + this.dx;
  this.y = ship.y + this.dy;
  this.d = 0;

  this.step = function(f) {
    ctx.beginPath();
    ctx.moveTo(this.x, this.y);
    ctx.lineTo(this.x += this.dx * f, this.y += this.dy * f);
    ctx.stroke();

    this.d += f;

    if (this.x < 0) { 
      this.x = w;
    } else if (this.x > w) {
      this.x = 0;
    }
    if (this.y < 0) {
      this.y = h;
    } else if (this.y > h) {
      this.y = 0;
    }
    this.d += f;
  }
}

/**
 * Insert an object at the first 'null' postion of an array.
 */
function insert (arr, obj) {
  var len = arr.length;
  for (var i = 0; i < len; i++) {
    if (arr[i] == null) {
      arr[i] = obj;
      return;
    }
  }
  arr.length = len + 1;
  arr[len] = obj;
}

/**
 * Constructors for polygons used as player object and asteroids.
 */
function Sprite(coords, size, x, y, r, dx, dy, dr) {
  this.coords = coords;
  this.hitrange = size * size;
  this.x = x;
  this.y = y;
  this.r = r;
  this.dx = dx;
  this.dy = dy;
  this.dr = dr;

  /**
   * Move and rotate object by the stored factor.
   */
  this.step = function(f) {
    var g = ctx;

    g.save();
    g.translate(this.x, this.y);
    g.rotate(this.r);
    g.beginPath();

    var c = this.coords;

    g.moveTo(c[0], c[1]);

    for (var i = c.length - 2; i >= 0; i = i - 2) {
      g.lineTo(c[i], c[i + 1]);
    }
    g.stroke();
    g.restore();

    this.x += this.dx * f;
    this.y += this.dy * f;
    this.r += this.dr * f;

    if (this.x > w + 10) this.x = -10;
    if (this.y > h + 10) this.y = -10;
    if (-10 > this.x) this.x = w + 10;
    if (-10 > this.y) this.y = h + 10;
  }
}

/**
 * Advance to the next level of the game
 */
function nextLevel() {
  rocks = new Array(2);
  level += 1;
  for (var i = 0; i < 0.6 + level / 2; i++) {
    var x0 = Math.random() * w * (i % 2);
    var y0 = Math.random() * h * ((i + 1) % 2);

    var dx = (Math.random() - 0.5) * base * (8 + level);
    var dy = (Math.random() - 0.5) * base * (8 + level);

    rocks[i] = new Sprite(big, 20 * base, x0, y0, Math.random() * 2 - 1,
        dx, dy, (Math.random() - 0.5) / 5);
  }
}

/**
 * Draw a text string at the given coordinates with the given align
 * ("right", "left" or "center").
 */
function drawString(x, y, text, align) {
  if (ctx.mozDrawText) {
    switch (align) {
    case "right":
      x -= ctx.mozMeasureText(text);
      break;
    case "center":
      x -= ctx.mozMeasureText(text) / 2;
      break;
    }
    ctx.translate(x, y);
    ctx.mozDrawText(text);
    ctx.translate(-x, -y);
  }
}

/**
 * Add a split rock fragment.
 */
function addSplit(r, dir) {
  insert(rocks, new Sprite(small, 10 * base, r.x, r.y, r.r,
      r.dx + r.dy * dir, r.dy - r.dx * dir, 2 * r.dr * dir));
}

/**
 * Determine if any rock is at the given (shot) position.
 */
function anyHit(sx, sy) {
  for (var j = rocks.length - 1; j >= 0; j -= 1) {
    var r = rocks[j];
    if (r != null) {
      var dx = r.x - sx;
      var dy = r.y - sy;

      if (dx * dx + dy * dy < r.hitrange) {
        rocks[j] = null;
        if (r.coords == big) {
          score += 10;
          addSplit(r, 1);
          addSplit(r, -1);
          if (Math.random > 2 / level) {
             addSplit(r, (Math.random() - 0.5) * 1.5);
          }
        } else{
          score += 50;
        }
        if (score > highscore) {
          highscore = score;
        }
        return true;
      }
    }
  }
  return false;
}

/**
 * Perform a game step.
 */
function step() {
  var time = new Date();
  var dt = time - lastStep;
  var factor = dt / 50;
  lastStep = time;

  ctx.fillStyle = "#000000";
  ctx.strokeStyle = "#00ff00";
  ctx.fillRect(0, 0, w, h);

  if (lives > 0 && Math.floor(blink) % 4 < 2) {
    ship.step(factor);
  }

  if (blink > 0) {
    blink += factor;
    if (blink > 50) {
      blink = 0;
    }
  }

  var sx = ship.x;
  var sy = ship.y;
  var any = false;

  for (var i = rocks.length - 1; i >= 0; i -= 1) {
    var r = rocks[i];
    if (r != null) {
      any = true;
      var dx = r.x - sx;
      var dy = r.y - sy;

      if (dx * dx + dy * dy < r.hitrange && blink == 0 && lives > 0) {
        blink = 1;
        lives -= 1;
        ship.x = w / 2;
        ship.y = h / 2;
        ship.dx = 0;
        ship.dy = 0;
      }
      r.step(factor);
    }
  }
  if (!any) {
    nextLevel();
  }

  for (var i = shots.length - 1; i >= 0; i -= 1) {
    var shot = shots[i];
    if (shot != null) {
      if (anyHit(shot.x, shot.y) || shot.d > 30) {
        shots[i] = null;
      } else{
        shot.step(factor);
      }
    }
  }

  ctx.fillStyle = "#00ff00";
  drawString(0, 20, "" + score, "left");
  drawString(w, 20, "HI " + highscore, "right");
  drawString(w, h - 5, "Ships: " + lives, "right");

  if (lives == 0) {
     drawString(w / 2, h / 2 - 10, "GAME OVER", "center");
     if (blink == 0) {
       drawString(w / 2, h / 2 + 10, "Press any key", "center");
     }
  }

  if (ship.resetDr) {
    ship.dr = 0;
  }
  setTimeout(step, Math.max(30 - dt, 10));
}

document.onkeydown = function(e) {
  var keycode = e.keyCode;

  if (lives == 0) {
    if (blink == 0) {
      lives = 3;
      score = 0;
      level = 0;
      nextLevel();
    }
    return;
  }

  if (keycode == 37) { // left=rotate left
    ship.dr = -0.2;
    ship.resetDr = false;
  } else if (keycode == 39) { // right=rotate right
    ship.dr = 0.2;
    ship.resetDr = false;
  } else if (keycode == 38 || keycode == 40) { // up or down keys?
    if (keycode == 38) {  // forward
      ship.dx += Math.cos(ship.r) * base;
      ship.dy += Math.sin(ship.r) * base;
    } else {  // back
      ship.dx -= Math.cos(ship.r) * base;
      ship.dy -= Math.sin(ship.r) * base;
    }
    if (Math.abs(ship.dx) < base / 2 && Math.abs(ship.dy) < base / 2) {
      ship.dx = 0;
      ship.dy = 0;
    }
  } else if (keycode == 52 && blink == 0) { // fire
    ship.x = Math.random() * w;
    ship.y = Math.random() * h;
    ship.dx = 0;
    ship.dy = 0;
    blink = 1;
  } else {
    insert(shots, new Shot());
  }
};

document.onkeyup = function(e) {
  var keycode = e.keyCode;
  if (keycode == 37 || keycode == 39) { 
    ship.resetDr = true;
  }
}

lastStep = new Date();
setTimeout(step, 30);
