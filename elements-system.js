// Elements system for Kids Study Tracker
// Focus Time unlock system and card display

// ===== FOCUS TIME UNLOCK SYSTEM =====
var _focusTimeHours = 0;
var _unlockedCount = 0;
var _elementOrder = ["H","He","Li","Be","B","C","N","O","F","Ne","Na","Mg","Al","Si","P","S","Cl","Ar","K","Ca","Sc","Ti","V","Cr","Mn","Fe","Co","Ni","Cu","Zn","Ga","Ge","As","Se","Br","Kr","Rb","Sr","Y","Zr","Nb","Mo","Tc","Ru","Rh","Pd","Ag","Cd","In","Sn","Sb","Te","I","Xe","Cs","Ba","La","Ce","Pr","Nd","Pm","Sm","Eu","Gd","Tb","Dy","Ho","Er","Tm","Yb","Lu","Hf","Ta","W","Re","Os","Ir","Pt","Au","Hg","Tl","Pb","Bi","Po","At","Rn","Fr","Ra","Ac","Th","Pa","U","Np","Pu","Am","Cm","Bk","Cf","Es","Fm","Md","No","Lr","Rf","Db","Sg","Bh","Hs","Mt","Ds","Rg","Cn","Nh","Fl","Mc","Lv","Ts","Og"];

var ELEM_CAT_COLORS = {
  "nonmetal":{bg:"#4ECDC4",fg:"#FFF"},
  "noble_gas":{bg:"#A78BFA",fg:"#FFF"},
  "alkali_metal":{bg:"#FF6B35",fg:"#FFF"},
  "alkaline_earth":{bg:"#F472B6",fg:"#FFF"},
  "transition_metal":{bg:"#667EEA",fg:"#FFF"},
  "halogen":{bg:"#06D6A0",fg:"#FFF"},
  "lanthanide":{bg:"#FFD23F",fg:"#333"},
  "actinide":{bg:"#F472B6",fg:"#FFF"},
  "post_transition":{bg:"#4ECDC4",fg:"#FFF"},
  "metalloid":{bg:"#667EEA",fg:"#FFF"}
};

function loadFocusTime(){
  var saved = localStorage.getItem('focusTimeHours');
  if(saved) _focusTimeHours = parseFloat(saved);

  // Calculate from historical logs if no saved value
  if(!_focusTimeHours && S.logs){
    var totalMin = 0;
    for(var i=0;i<S.logs.length;i++){
      totalMin += S.logs[i].dur || 0;
    }
    _focusTimeHours = totalMin / 60;
  }

  var savedCount = localStorage.getItem('unlockedCount');
  if(savedCount) _unlockedCount = parseInt(savedCount);
  else _unlockedCount = Math.floor(_focusTimeHours / 4);
}

function saveFocusTime(){
  localStorage.setItem('focusTimeHours', _focusTimeHours.toString());
  localStorage.setItem('unlockedCount', _unlockedCount.toString());
}

function addFocusTime(minutes){
  _focusTimeHours += minutes / 60;
  saveFocusTime();
  checkNewUnlocks();
}

function checkNewUnlocks(){
  var newCount = Math.floor(_focusTimeHours / 4);
  if(newCount > _unlockedCount){
    _unlockedCount = newCount;
    saveFocusTime();
    showUnlockNotification();
  }
}

function showUnlockNotification(){
  var nextElement = _elementOrder[_unlockedCount - 1];
  if(nextElement && ELEMENTS[nextElement]){
    var elem = ELEMENTS[nextElement];
    var notif = document.createElement('div');
    notif.style.cssText = 'position:fixed;top:20px;right:20px;background:linear-gradient(135deg,#FFD23F,#FF6B35);color:white;padding:20px;border-radius:12px;box-shadow:0 4px 20px rgba(0,0,0,0.3);z-index:10000;animation:slideIn 0.5s';
    notif.innerHTML = '<div style="font-size:18px;font-weight:800;margin-bottom:8px"> 新元素解锁！</div><div style="font-size:14px">解锁了 '+elem.name_zh+' ('+elem.name_en+')</div><div style="font-size:12px;margin-top:8px">点击 ELEMENTS 标签答题赢取 +10 XP！</div>';
    document.body.appendChild(notif);
    setTimeout(function(){
      notif.style.animation = 'slideOut 0.5s';
      setTimeout(function(){notif.remove();}, 500);
    }, 5000);
  }
}

function isElementUnlocked(symbol){
  var index = _elementOrder.indexOf(symbol);
  return index < _unlockedCount;
}

// ===== TEST FUNCTIONS =====
function testAddFocusTime(hours){
  _focusTimeHours += hours;
  saveFocusTime();
  checkNewUnlocks();
  if(typeof drawElements === 'function') drawElements();
  console.log('Added '+hours+' hours. Total: '+_focusTimeHours.toFixed(2)+' hours. Unlocked: '+_unlockedCount);
}

function resetFocusTime(){
  _focusTimeHours = 0;
  _unlockedCount = 0;
  localStorage.removeItem('focusTimeHours');
  localStorage.removeItem('unlockedCount');
  if(typeof drawElements === 'function') drawElements();
  console.log('Focus time reset');
}

function checkUnlocks(){
  console.log('=== ELEMENT UNLOCK STATUS ===');
  console.log('Total Focus Time: '+_focusTimeHours.toFixed(2)+' hours');
  console.log('Unlocked: '+_unlockedCount+' elements');
  console.log('');
  console.log('Next unlocks:');
  for(var i=_unlockedCount;i<Math.min(_unlockedCount+5,_elementOrder.length);i++){
    var elem = ELEMENTS[_elementOrder[i]];
    var hoursNeeded = (i+1)*4;
    console.log((i+1)+'. '+elem.name_zh+' ('+elem.name_en+') - need '+hoursNeeded+' hours total');
  }
}

// ===== DRAW ELEMENTS =====
function drawElements(){
  loadFocusTime();
  var el = document.getElementById('ac-elements');
  if(!el) return;
  var html = '<h2 style="margin-bottom:16px;font-family:LXGW WenKai">⚗️ 元素周期表</h2>';
  html += '<div style="margin-bottom:16px;padding:12px;background:linear-gradient(135deg,#667EEA,#764BA2);border-radius:12px;color:white">';
  html += '<div style="font-size:14px;font-weight:700;margin-bottom:8px">🎯 Focus Time: '+_focusTimeHours.toFixed(1)+' 小时</div>';
  html += '<div style="font-size:12px">已解锁 '+_unlockedCount+' 张卡片 | 下一张还需 '+(4 - (_focusTimeHours % 4)).toFixed(1)+' 小时</div>';
  html += '<div style="font-size:10px;margin-top:8px;opacity:0.8">提示：在控制台输入 checkUnlocks() 查看解锁状态</div>';
  html += '</div>';
  html += '<div id="periodic-table-grid" style="display:grid;grid-template-columns:repeat(18,minmax(44px,1fr));gap:3px;min-width:850px;overflow-x:auto">';

  for(var r=1; r<=7; r++){
    for(var c=1; c<=18; c++){
      var found = false;
      for(var s in ELEMENTS){
        var e = ELEMENTS[s];
        if(e.row === r && e.col === c){
          var unlocked = isElementUnlocked(s);
          var catColor = ELEM_CAT_COLORS[e.category] || {bg:'#999',fg:'#FFF'};

          if(unlocked){
            // Unlocked - show full color
            html += '<div class="elem-cell" data-symbol="'+s+'" style="background:'+catColor.bg+';color:'+catColor.fg+';border-radius:6px;padding:4px;cursor:pointer;aspect-ratio:1;border:1.5px solid '+catColor.bg+'">';
            html += '<div style="position:absolute;top:3px;right:5px;font-size:9px;font-weight:600;opacity:0.9;line-height:1">'+e.atomic_mass+'</div>';
            html += '<div style="font-size:36px;font-weight:900;line-height:1;margin-top:6px">'+s+'</div>';
            html += '<div style="font-size:13px;font-weight:800;margin-top:3px;line-height:1">'+e.name_zh+'</div>';
            html += '<div style="font-size:8px;opacity:0.8;margin-top:1px">'+(e.name_pinyin||'')+'</div>';
            html += '</div>';
          } else {
            // Locked - show gray with symbol
            html += '<div class="elem-cell" data-symbol="'+s+'" style="background:#ddd;color:#999;border-radius:6px;padding:4px;aspect-ratio:1;border:1.5px solid #ccc;cursor:not-allowed">';
            html += '<div style="font-size:24px;font-weight:900;line-height:1;margin-top:6px;opacity:0.5">'+s+'</div>';
            html += '<div style="font-size:10px;font-weight:700;margin-top:3px;line-height:1;opacity:0.5">'+e.name_zh+'</div>';
            html += '<div style="font-size:16px;margin-top:2px">🔒</div>';
            html += '</div>';
          }
          found = true;
          break;
        }
      }
      if(!found){
        html += '<div style="aspect-ratio:1"></div>';
      }
    }
  }
  html += '</div>';
  el.innerHTML = html;

  setTimeout(function(){
    var cells = document.querySelectorAll('.elem-cell[data-symbol]');
    cells.forEach(function(cell){
      var symbol = cell.getAttribute('data-symbol');
      if(isElementUnlocked(symbol)){
        cell.addEventListener('click',function(){
          openElemCard(symbol);
        });
      }
    });
  },100);
}

// ===== ELEMENT CARD =====
var _currentSymbol = null;
var _quizState = {q:0,score:0,answered:false};

function openElemCard(symbol){
  var elem = ELEMENTS[symbol];
  if(!elem) return;
  _currentSymbol = symbol;
  _quizState = {q:0,score:0,answered:false};

  var modal = document.getElementById('elem-modal');
  if(!modal){
    modal = document.createElement('div');
    modal.id = 'elem-modal';
    modal.style.cssText = 'position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,0.5);z-index:9999;align-items:center;justify-content:center;padding:20px;display:none';
    modal.innerHTML = '<div style="max-width:500px;width:95vw;background:white;border-radius:16px;padding:20px;position:relative"><div id="elem-card-inner" style="position:relative;width:100%;aspect-ratio:3/4"><div id="elem-badge-front" style="position:absolute;inset:0;cursor:pointer"></div><div id="elem-card-back-content" style="position:absolute;inset:0;overflow-y:auto;padding:20px;display:none"></div></div><button onclick="closeElemModal()" style="margin-top:10px;padding:8px 16px">CLOSE</button></div>';
    document.body.appendChild(modal);

    document.getElementById('elem-badge-front').addEventListener('click', function(){
      showBack(ELEMENTS[_currentSymbol]);
    });
  }

  showFront();
  modal.style.display = 'flex';
}

function showFront(){
  var elem = ELEMENTS[_currentSymbol];
  if(!elem) return;
  var catColor = ELEM_CAT_COLORS[elem.category] || {bg:'#999',fg:'#FFF'};
  var html = '<div style="width:100%;height:100%;background:linear-gradient(135deg,'+catColor.bg+','+catColor.bg+');color:#FFF;display:flex;flex-direction:column;padding:20px;border-radius:16px;justify-content:center;align-items:center">';
  html += '<div style="font-size:72px;font-weight:900">'+elem.symbol+'</div>';
  html += '<div style="font-size:24px;font-weight:700;margin-top:10px">'+elem.name_en+'</div>';
  html += '<div style="font-size:18px;margin-top:5px">'+elem.name_zh+'</div>';
  html += '<div style="font-size:12px;margin-top:20px;opacity:0.8">点击翻转 →</div>';
  html += '</div>';
  document.getElementById('elem-badge-front').innerHTML = html;
  document.getElementById('elem-badge-front').style.display = 'block';
  document.getElementById('elem-card-back-content').style.display = 'none';
}

function showBack(elem){
  var html = '<h3 style="text-align:center;margin-bottom:15px">'+elem.name_en+' · '+elem.name_zh+'</h3>';

  if(elem.properties){
    html += '<div style="margin-bottom:10px"><strong>特性</strong></div>';
    html += '<div style="display:flex;flex-wrap:wrap;gap:5px;margin-bottom:15px">';
    elem.properties.forEach(function(p){
      html += '<span style="background:#FFF0F0;color:#C0392B;padding:5px 10px;border-radius:8px;font-size:12px">'+p.icon+' '+p.text+'</span>';
    });
    html += '</div>';
  }

  if(elem.intro){
    html += '<div style="margin-bottom:10px"><strong>简介</strong></div>';
    html += '<div style="font-size:13px;line-height:1.5;margin-bottom:15px">'+elem.intro+'</div>';
  }

  if(elem.amazing_facts){
    html += '<div style="margin-bottom:10px"><strong>有趣事实</strong></div>';
    elem.amazing_facts.forEach(function(f,i){
      html += '<div style="background:#FFF9E6;padding:8px;border-radius:6px;margin-bottom:5px;font-size:12px">#'+(i+1)+' '+f+'</div>';
    });
    html += '<div style="margin-top:15px"></div>';
  }

  if(elem.english_fun){
    html += '<div style="background:#EDE7F6;padding:10px;border-radius:8px;font-size:12px;color:#4527A0;margin-bottom:15px">'+elem.english_fun+'</div>';
  }

  // Quiz
  if(elem.quiz && elem.quiz.length){
    html += '<div style="text-align:center;color:#C0392B;font-size:12px">翻回正面答题赢取 XP！</div>';
  }

  document.getElementById('elem-card-back-content').innerHTML = html;
  document.getElementById('elem-badge-front').style.display = 'none';
  document.getElementById('elem-card-back-content').style.display = 'block';
}

function closeElemModal(){
  var modal = document.getElementById('elem-modal');
  if(modal) modal.style.display = 'none';
}

// Add to drawCurrent
var _origDrawCurrent = typeof drawCurrent !== 'undefined' ? drawCurrent : null;
drawCurrent = function(){
  if(_origDrawCurrent) _origDrawCurrent();
  var g = document.querySelector('.page-group.active');
  if(g && g.id === 'pg-elements' && typeof drawElements === 'function') drawElements();
};

console.log("Elements system loaded");
