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
 * @fileoverview MiniJoe Example: Simple function plotter
 * @author Stefan Haustein
 */

// toggle comments for alternative functions
// fn = function(x, y) {return Math.sin(x) * Math.cos(y);};
fn = function(x, y) {
  var r = (Math.sqrt(x * x + y * y) + 0.0001) * 1.41;
  return Math.sin(r) / r * 5;
};

var canvas = document.getElementById("canvas");
var w = window.innerWidth;
var h = window.innerHeight;

canvas.height = h;
canvas.width = w;

var range = 5;
var scale = Math.min(w, h) / range / 2;

var ctx = canvas.getContext("2d");

ctx.translate(w / 2, h / 2);

ctx.fillStyle = "#ffffff";
ctx.strokeStyle = "#000000";

ctx.moveTo (-w / 2, 0);
ctx.lineTo (w / 2, 0);
ctx.moveTo (0, -h / 2);
ctx.lineTo (0, h / 2);

ctx.stroke();

var delta = range / 20;

for (var y = -range; y <= range; y += delta) {
  ctx.moveTo((-5 + y / 3) * scale, -(fn(-range, y) + y / 3) * scale);
  for (var x = -range; x <= range; x += delta) {
    ctx.lineTo((x + y / 3) * scale, -(fn(x, y) + y / 3) * scale);
  }
}

ctx.stroke();
