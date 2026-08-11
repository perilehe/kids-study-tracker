# Custom Milestone Feature Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add self-service challenge milestones to the HTML study tracker where users define their own goals, deadlines, and XP/box rewards.

**Architecture:** Add a new `custom_milestones` Supabase table. Insert CRUD functions, modal HTML, and UI rendering into `app.html` following existing patterns (minified JS style, `S.*` state, `sb.from()` queries, `hudov/hudbx` modals).

**Tech Stack:** Vanilla JS (ES5 compatible), Supabase JS SDK v2, HTML/CSS — all in single file `app.html`

**Spec:** `docs/superpowers/specs/2026-08-11-custom-milestone-design.md`

---

## File Map

All changes are in a single file:

| Location in `app.html` | What to add |
|------------------------|-------------|
| After line 620 (after `add-rw-modal` div) | Two new modal HTML blocks: create-challenge modal, claim-note modal |
| Line ~2090 (before `loadAll()`) | `S.customMs = [];` state init |
| Lines 2101-2103 (inside `loadAll()`) | Load custom milestones + expiration check |
| After line 2123 (after existing save functions) | 4 CRUD functions for custom milestones |
| Lines 2264-2265 (`drawSettings()`) | Append "MY CHALLENGES" section after existing milestones |
| After line 2265 (after `drawSettings`) | New functions: `drawCustomMs()`, `openCreateChallenge()`, `confirmCreateChallenge()`, `claimChallenge()`, `confirmClaimChallenge()`, `abandonChallenge()`, `deleteChallenge()`, `fmtCountdown()` |
| Line ~2271 (`resetData()`) | Add `custom_milestones` cleanup |
| Lines 2273-2279 (INIT section) | Add click-handler for new modals |

---

### Task 1: Create Supabase Table

**Files:**
- Supabase SQL Editor (manual)

- [ ] **Step 1: Run SQL in Supabase dashboard**

Go to Supabase → SQL Editor → New Query → paste:

```sql
CREATE TABLE custom_milestones (
  id SERIAL PRIMARY KEY,
  title TEXT NOT NULL,
  description TEXT,
  xp_reward INTEGER DEFAULT 0,
  box_reward INTEGER DEFAULT 0,
  deadline TIMESTAMP NOT NULL,
  status TEXT DEFAULT 'active',
  completion_note TEXT,
  completed_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT NOW()
);

-- Allow the app's RLS policy to access the table
ALTER TABLE custom_milestones ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow all for app" ON custom_milestones
  FOR ALL USING (true) WITH CHECK (true);
```

- [ ] **Step 2: Verify table exists**

Run in SQL Editor:

```sql
SELECT * FROM custom_milestones LIMIT 1;
```

Expected: Empty result, no error.

- [ ] **Step 3: Commit placeholder**

```bash
cd E:/KidsStudyTracker
git commit --allow-empty -m "feat: create custom_milestones table in Supabase"
```

---

### Task 2: Add State Init and CRUD Functions

**Files:**
- Modify: `app.html:2090` (before `loadAll()`)
- Modify: `app.html:2113-2123` (after existing save functions)

- [ ] **Step 1: Add state initialization**

In `app.html`, find the line right before `/* LOAD FROM SUPABASE */` (around line 2090). Insert before it:

```js
S.customMs=[];
```

- [ ] **Step 2: Add CRUD functions**

Find the line `async function updateRewardRow(id,data){try{await sb.from('rewards').update(data).eq('id',id)}catch(e){}}` (around line 2124). Insert immediately AFTER it:

```js
async function loadCustomMs(){try{var{data:cm}=await sb.from('custom_milestones').select().order('created_at',{ascending:false});S.customMs=cm||[];for(var i=0;i<S.customMs.length;i++){if(S.customMs[i].status==='active'&&new Date(S.customMs[i].deadline)<new Date()){S.customMs[i].status='expired';try{await sb.from('custom_milestones').update({status:'expired'}).eq('id',S.customMs[i].id)}catch(e){}}}}catch(e){S.customMs=[]}}
async function saveCustomMs(m){try{var r=await sb.from('custom_milestones').insert({title:m.title,description:m.description||'',xp_reward:m.xp_reward||0,box_reward:m.box_reward||0,deadline:m.deadline}).select();return r}catch(e){return null}}
async function updateCustomMs(id,data){try{await sb.from('custom_milestones').update(data).eq('id',id)}catch(e){}}
async function deleteCustomMs(id){try{await sb.from('custom_milestones').delete().eq('id',id)}catch(e){}}
```

- [ ] **Step 3: Add loadCustomMs() call to loadAll()**

Find the line inside `loadAll()` that reads:
```
var{data:tm}=await sb.from('today_missions').select();S.tm=tm||[];for(var i=0;i<S.tm.length;i++){if(S.tm[i].done==='true')S.tm[i].done=true;if(S.tm[i].done==='false')S.tm[i].done=false}
```

Insert immediately AFTER it:

```js
await loadCustomMs();
```

- [ ] **Step 4: Verify no JS errors**

Open `app.html` in browser → open DevTools console → verify no errors on page load.

- [ ] **Step 5: Commit**

```bash
git add app.html
git commit -m "feat: add custom milestone CRUD functions and state"
```

---

### Task 3: Add Modal HTML

**Files:**
- Modify: `app.html:620` (after `add-rw-modal` closing `</div></div>`)

- [ ] **Step 1: Insert create-challenge modal and claim-note modal**

Find the closing of `add-rw-modal` — the line ending with `</div></div>` right before `<div class="hudov" id="redeem-modal">` (around line 620-621). Insert between them:

```html
<div class="hudov" id="create-ms-modal"><div class="hudbx" style="text-align:left">
  <div class="hudt" style="text-align:center">🎯 NEW CHALLENGE</div>
  <div class="ig"><label class="il">Title</label><input class="if" id="cms-title" placeholder="e.g. 一分钟跳绳100个"></div>
  <div class="ig"><label class="il">Description</label><textarea class="if" id="cms-desc" rows="3" placeholder="Describe the challenge rules..." style="resize:vertical;font-family:inherit"></textarea></div>
  <div style="display:flex;gap:10px">
    <div class="ig" style="flex:1"><label class="il">XP Reward</label><input type="number" class="if" id="cms-xp" placeholder="50" min="0"></div>
    <div class="ig" style="flex:1"><label class="il">Box Reward</label><input type="number" class="if" id="cms-box" placeholder="1" min="0"></div>
  </div>
  <div class="ig"><label class="il">Deadline</label>
    <div style="display:flex;gap:6px;flex-wrap:wrap" id="cms-deadline-btns"></div>
    <div id="cms-custom-days" style="display:none;margin-top:8px"><input type="number" class="if" id="cms-days" placeholder="Enter days" min="1" style="width:100px;display:inline-block"></div>
  </div>
  <button class="btn bp" onclick="confirmCreateChallenge()">CREATE 🎯</button>
  <button class="btn bs" onclick="closeHUD('create-ms-modal')">Cancel</button>
</div></div>
<div class="hudov" id="claim-ms-modal"><div class="hudbx" style="text-align:left">
  <div class="hudt" style="text-align:center">✅ CLAIM CHALLENGE</div>
  <div id="claim-ms-title" style="font-size:13px;font-weight:800;margin-bottom:8px;color:var(--text)"></div>
  <div class="ig"><label class="il">Completion Note (required)</label><textarea class="if" id="claim-ms-note" rows="3" placeholder="What did you achieve? How did it go?" style="resize:vertical;font-family:inherit"></textarea></div>
  <button class="btn bp" onclick="confirmClaimChallenge()">CONFIRM ✅</button>
  <button class="btn bs" onclick="closeHUD('claim-ms-modal')">Cancel</button>
</div></div>
```

- [ ] **Step 2: Add click-handler for new modals in INIT section**

Find the INIT section (around line 2273-2279). Find the line:
```js
var rdEl=document.getElementById("redeem-modal");if(rdEl)rdEl.addEventListener("click",function(e){if(e.target===this&&!bbPicking)this.classList.remove("on")});
```

Insert immediately AFTER it:

```js
var cmsEl=document.getElementById("create-ms-modal");if(cmsEl)cmsEl.addEventListener("click",function(e){if(e.target===this)this.classList.remove("on")});
var clmEl=document.getElementById("claim-ms-modal");if(clmEl)clmEl.addEventListener("click",function(e){if(e.target===this)this.classList.remove("on")});
```

- [ ] **Step 3: Verify modals render**

Open `app.html` → Go to Settings tab → verify no JS errors. Modals are hidden by default (`.hudov` has `display:none`), but verify HTML is valid by inspecting the DOM.

- [ ] **Step 4: Commit**

```bash
git add app.html
git commit -m "feat: add create-challenge and claim-note modal HTML"
```

---

### Task 4: Add Custom Milestone UI Functions

**Files:**
- Modify: `app.html` — insert after `drawSettings()` function (around line 2265)

- [ ] **Step 1: Insert all custom milestone UI functions**

Find the line that starts with `/* PROFILE */` (around line 2267). Insert BEFORE it:

```js
/* ===== CUSTOM MILESTONES ===== */
var _cmsDeadlineDays=3;
var _claimTargetId=null;

function drawCustomMs(){
  if(!S.customMs)S.customMs=[];
  var html='';
  html+='<div style="display:flex;align-items:center;justify-content:space-between;margin:20px 0 8px">';
  html+='<div style="font-size:12px;font-weight:800;color:var(--accent);letter-spacing:.04em">🎯 MY CHALLENGES</div>';
  html+='<button class="fbtn active" onclick="openCreateChallenge()" style="font-size:9px;padding:4px 10px">+ CREATE</button>';
  html+='</div>';
  var active=[],done=[];
  for(var i=0;i<S.customMs.length;i++){
    if(S.customMs[i].status==='active')active.push(S.customMs[i]);
    else done.push(S.customMs[i]);
  }
  active.sort(function(a,b){return new Date(a.deadline)-new Date(b.deadline)});
  if(!active.length&&!done.length){
    html+='<div class="scard" style="text-align:center;padding:20px;color:var(--text3);font-size:11px">No challenges yet. Create one!</div>';
  }
  for(var i=0;i<active.length;i++){
    var m=active[i];
    var remaining=new Date(m.deadline)-new Date();
    var timeStr=fmtCountdown(remaining);
    var urgent=remaining<24*60*60*1000;
    html+='<div class="scard" style="'+(urgent?'border-color:#F43F5E;background:#FFF5F5':'')+'">';
    html+='<div style="display:flex;align-items:flex-start;gap:8px">';
    html+='<div style="font-size:18px;flex-shrink:0">🎯</div>';
    html+='<div style="flex:1">';
    html+='<div style="font-size:12px;font-weight:800;color:var(--text)">'+m.title+'</div>';
    if(m.description)html+='<div style="font-size:10px;color:var(--text3);margin-top:2px">'+m.description+'</div>';
    html+='<div style="display:flex;gap:12px;margin-top:6px;align-items:center">';
    html+='<span style="font-size:10px;font-weight:700;color:'+(urgent?'#F43F5E':'var(--text2)')+'">⏰ '+timeStr+'</span>';
    if(m.xp_reward>0)html+='<span style="font-size:10px;font-weight:700;color:var(--c1)">+'+m.xp_reward+' XP</span>';
    if(m.box_reward>0)html+='<span style="font-size:10px;font-weight:700;color:#8B5CF6">+'+m.box_reward+' Box</span>';
    html+='</div></div></div>';
    html+='<div style="display:flex;gap:8px;margin-top:8px">';
    html+='<button class="btn bp" style="flex:1;padding:8px;font-size:10px" onclick="claimChallenge('+m.id+')">CLAIM ✅</button>';
    html+='<button class="btn bs" style="flex:1;padding:8px;font-size:10px" onclick="abandonChallenge('+m.id+')">ABANDON ✗</button>';
    html+='</div></div>';
  }
  if(done.length){
    html+='<div style="margin-top:12px">';
    html+='<div style="font-size:10px;font-weight:700;color:var(--text3);cursor:pointer;letter-spacing:.04em" onclick="var e=document.getElementById(\'cm-done-list\');e.style.display=e.style.display===\'none\'?\'block\':\'none\'">COMPLETED / EXPIRED ('+done.length+') ▼</div>';
    html+='<div id="cm-done-list" style="display:none;margin-top:6px">';
    for(var i=0;i<done.length;i++){
      var m=done[i];
      var isExpired=m.status==='expired'||m.status==='abandoned';
      var icon=m.status==='completed'?'✅':'❌';
      var bgColor=m.status==='completed'?'#ECFDF5':'var(--card)';
      html+='<div class="scard" style="opacity:'+(isExpired?'0.6':'1')+';background:'+bgColor+'">';
      html+='<div style="display:flex;align-items:flex-start;gap:8px">';
      html+='<div style="font-size:16px;flex-shrink:0">'+icon+'</div>';
      html+='<div style="flex:1">';
      html+='<div style="font-size:11px;font-weight:800;color:var(--text)">'+m.title+'</div>';
      if(m.description)html+='<div style="font-size:9px;color:var(--text3);margin-top:2px">'+m.description+'</div>';
      if(m.status==='completed'){
        var cDate=m.completed_at?new Date(m.completed_at).toISOString().split('T')[0]:'';
        html+='<div style="font-size:9px;color:#059669;margin-top:4px">Completed: '+cDate+'</div>';
        if(m.completion_note)html+='<div style="font-size:9px;color:var(--text3);margin-top:2px;font-style:italic">"'+m.completion_note+'"</div>';
      }else{
        html+='<div style="font-size:9px;color:var(--text3);margin-top:4px;font-weight:700">'+m.status.toUpperCase()+'</div>';
      }
      if(m.xp_reward>0||m.box_reward>0){
        html+='<div style="display:flex;gap:8px;margin-top:4px">';
        if(m.xp_reward>0)html+='<span style="font-size:9px;color:var(--c1)">+'+m.xp_reward+' XP</span>';
        if(m.box_reward>0)html+='<span style="font-size:9px;color:#8B5CF6">+'+m.box_reward+' Box</span>';
        html+='</div>';
      }
      html+='</div></div>';
      if(isExpired)html+='<button class="btn bs" style="margin-top:6px;padding:6px;font-size:9px" onclick="deleteChallenge('+m.id+')">DELETE 🗑</button>';
      html+='</div>';
    }
    html+='</div></div>';
  }
  return html;
}

function fmtCountdown(ms){
  if(ms<=0)return'EXPIRED';
  var d=Math.floor(ms/(24*60*60*1000));
  var h=Math.floor((ms%(24*60*60*1000))/(60*60*1000));
  var m=Math.floor((ms%(60*60*1000))/(60*1000));
  if(d>0)return d+'d '+h+'h left';
  if(h>0)return h+'h '+m+'m left';
  return m+'m left';
}

function openCreateChallenge(){
  _cmsDeadlineDays=3;
  document.getElementById('cms-title').value='';
  document.getElementById('cms-desc').value='';
  document.getElementById('cms-xp').value='';
  document.getElementById('cms-box').value='';
  document.getElementById('cms-custom-days').style.display='none';
  document.getElementById('cms-days').value='';
  var opts=[{l:'1D',d:1},{l:'3D',d:3},{l:'7D',d:7},{l:'14D',d:14},{l:'30D',d:30},{l:'Custom',d:0}];
  var h='';
  for(var i=0;i<opts.length;i++){
    var act=opts[i].d===_cmsDeadlineDays?'background:var(--text);color:#FFF;border-color:var(--text)':'';
    h+='<button class="fbtn" style="'+act+'" onclick="_cmsPickDeadline('+opts[i].d+',this)">'+opts[i].l+'</button>';
  }
  document.getElementById('cms-deadline-btns').innerHTML=h;
  document.getElementById('create-ms-modal').classList.add('on');
}

function _cmsPickDeadline(d,el){
  _cmsDeadlineDays=d;
  var btns=document.getElementById('cms-deadline-btns').querySelectorAll('.fbtn');
  for(var i=0;i<btns.length;i++)btns[i].style.cssText='';
  if(el){el.style.cssText='background:var(--text);color:#FFF;border-color:var(--text)'}
  document.getElementById('cms-custom-days').style.display=d===0?'block':'none';
}

async function confirmCreateChallenge(){
  var title=document.getElementById('cms-title').value.trim();
  if(!title){hud('Please enter a title');return}
  var desc=document.getElementById('cms-desc').value.trim();
  var xp=parseInt(document.getElementById('cms-xp').value)||0;
  var box=parseInt(document.getElementById('cms-box').value)||0;
  if(xp<=0&&box<=0){hud('Set at least one reward (XP or Box)');return}
  var days=_cmsDeadlineDays;
  if(days===0){days=parseInt(document.getElementById('cms-days').value)||0;if(days<=0){hud('Enter a valid number of days');return}}
  var deadline=new Date();deadline.setDate(deadline.getDate()+days);
  var m={title:title,description:desc,xp_reward:xp,box_reward:box,deadline:deadline.toISOString()};
  var saved=await saveCustomMs(m);
  if(saved&&saved[0]){S.customMs.unshift(saved[0]);saved[0].status='active'}
  closeHUD('create-ms-modal');
  snd([523,659,784],"sine",0.2);
  drawSettings();
}

function claimChallenge(id){
  _claimTargetId=id;
  var m=null;for(var i=0;i<S.customMs.length;i++)if(S.customMs[i].id===id){m=S.customMs[i];break}
  if(!m)return;
  document.getElementById('claim-ms-title').textContent=m.title;
  document.getElementById('claim-ms-note').value='';
  document.getElementById('claim-ms-modal').classList.add('on');
}

async function confirmClaimChallenge(){
  var note=document.getElementById('claim-ms-note').value.trim();
  if(!note){hud('Please write a completion note');return}
  var m=null;for(var i=0;i<S.customMs.length;i++)if(S.customMs[i].id===_claimTargetId){m=S.customMs[i];break}
  if(!m){closeHUD('claim-ms-modal');return}
  var now=new Date().toISOString();
  await updateCustomMs(m.id,{status:'completed',completion_note:note,completed_at:now});
  m.status='completed';m.completion_note=note;m.completed_at=now;
  if(m.xp_reward>0)addXP(m.xp_reward);
  for(var b=0;b<(m.box_reward||0);b++){
    var nr={nm:"MYSTERIOUS BOX",cn:"神秘宝箱",cost:0,status:"UNSPENT",ts:Date.now(),ua:null,_milestone:"custom_"+m.id,_msDesc:m.title};
    var saved=await saveLoot(nr);
    if(saved&&saved[0])nr.id=saved[0].id;else nr.id="loc"+Date.now()+b;
    S.loot.unshift(nr);
  }
  closeHUD('claim-ms-modal');
  snd([523,659,784,1047],"sine",0.3);
  hud("CHALLENGE COMPLETE!",[{l:"AWESOME!",s:"bp",f:function(){}}]);
  drawSettings();
  updXP();
}

function abandonChallenge(id){
  hud("Abandon this challenge?",[
    {l:"ABANDON",s:"bp",f:async function(){
      await updateCustomMs(id,{status:'abandoned'});
      for(var i=0;i<S.customMs.length;i++)if(S.customMs[i].id===id){S.customMs[i].status='abandoned';break}
      drawSettings();
    }},
    {l:"CANCEL",s:"bs",f:function(){}}
  ],true);
}

async function deleteChallenge(id){
  await deleteCustomMs(id);
  S.customMs=S.customMs.filter(function(m){return m.id!==id});
  drawSettings();
}
```

- [ ] **Step 2: Verify functions exist**

Open `app.html` → DevTools console → type `typeof drawCustomMs` → Expected: `"function"`

- [ ] **Step 3: Commit**

```bash
git add app.html
git commit -m "feat: add custom milestone UI functions"
```

---

### Task 5: Integrate into drawSettings()

**Files:**
- Modify: `app.html:2265` (`drawSettings()` function)

- [ ] **Step 1: Add custom milestones to drawSettings()**

Find the `drawSettings()` function. It ends with:
```js
html+='<button class="btn" style="background:#FEF2F2;color:#F43F5E;border:1px solid #FECACA" onclick="resetData()">Reset All Data</button>';el.innerHTML=html}
```

Insert BEFORE the `el.innerHTML=html}` part (i.e., before the Danger Zone section). Find this exact string:
```js
html+='<div style="font-size:14px;font-weight:800;color:#F43F5E;margin:20px 0 12px">Danger Zone</div>';
```

Insert immediately BEFORE it:

```js
html+=drawCustomMs();
```

- [ ] **Step 2: Verify in browser**

Open `app.html` → Settings tab → verify:
1. "🎯 MY CHALLENGES" section appears below built-in milestones
2. "+ CREATE" button is visible
3. "No challenges yet. Create one!" message shows

- [ ] **Step 3: Commit**

```bash
git add app.html
git commit -m "feat: integrate custom milestones into Settings page"
```

---

### Task 6: Add Reset Cleanup

**Files:**
- Modify: `app.html:2271` (`resetData()` function)

- [ ] **Step 1: Add custom_milestones cleanup to resetData()**

Find the `resetData()` function. It contains:
```js
await sb.from('today_missions').delete().neq('id',0);S.logs=[];S.loot=[];S.pm=[];S.tm=[];
```

Insert `S.customMs=[];` right after `S.tm=[];` and add `await sb.from('custom_milestones').delete().neq('id',0);` right before it. The full modified line should be:

```js
await sb.from('today_missions').delete().neq('id',0);await sb.from('custom_milestones').delete().neq('id',0);S.logs=[];S.loot=[];S.pm=[];S.tm=[];S.customMs=[];
```

- [ ] **Step 2: Commit**

```bash
git add app.html
git commit -m "feat: add custom_milestones cleanup to resetData()"
```

---

### Task 7: End-to-End Manual Testing

**Files:** `app.html` (no code changes — testing only)

- [ ] **Step 1: Test CREATE flow**

1. Open `app.html` → Settings tab
2. Click "+ CREATE"
3. Fill in: Title="一分钟跳绳100个", Description="连续跳1分钟不中断", XP=50, Box=1, Deadline=3D
4. Click "CREATE 🎯"
5. Verify: Modal closes, card appears with countdown, XP and Box rewards shown
6. Check Supabase: `SELECT * FROM custom_milestones` — should show 1 row

- [ ] **Step 2: Test CLAIM flow**

1. Click "CLAIM ✅" on the active challenge card
2. Verify: Claim modal opens with title shown
3. Try clicking CONFIRM without filling note → verify error "Please write a completion note"
4. Fill in note: "跳了120个！"
5. Click "CONFIRM ✅"
6. Verify:
   - Celebration HUD appears
   - Card changes to completed state (green ✅)
   - XP increased by 50 (check top bar)
   - 1 mysterious box added to inventory (check Rewards → Inventory)
7. Check Supabase: status='completed', completion_note filled, completed_at set

- [ ] **Step 3: Test ABANDON flow**

1. Create another challenge: Title="100 pushups", XP=30, Box=0, Deadline=7D
2. Click "ABANDON ✗"
3. Verify: Warning HUD appears
4. Click "ABANDON"
5. Verify: Card moves to "COMPLETED / EXPIRED" section, shows "ABANDONED"
6. Verify: No XP or boxes awarded

- [ ] **Step 4: Test EXPIRE flow**

1. Create a challenge with Deadline=1D
2. In Supabase, manually set its deadline to yesterday:
   ```sql
   UPDATE custom_milestones SET deadline = NOW() - INTERVAL '1 day' WHERE status = 'active';
   ```
3. Reload `app.html`
4. Verify: Challenge shows as "EXPIRED" in collapsed section
5. Click "DELETE 🗑" → verify it's removed

- [ ] **Step 5: Test edge cases**

1. Create with XP=0, Box=1 → verify it works (box only)
2. Create with XP=100, Box=0 → verify it works (XP only)
3. Try creating with XP=0, Box=0 → verify error "Set at least one reward"
4. Create with Custom deadline=5 days → verify countdown shows ~5 days
5. Reload page → verify all data persists

- [ ] **Step 6: Test Reset**

1. Click "Reset All Data" → confirm
2. Verify: page reloads, custom milestones gone
3. Check Supabase: `SELECT * FROM custom_milestones` → empty

- [ ] **Step 7: Final commit**

```bash
git add app.html
git commit -m "fix: any fixes found during manual testing"
```

(Only commit if changes were needed. If all tests pass, skip.)
