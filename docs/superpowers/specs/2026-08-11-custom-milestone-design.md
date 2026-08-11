# Custom Milestone Feature Design

**Date:** 2026-08-11
**Status:** Draft
**Platform:** HTML version (app.html) only
**Deployment:** Local testing only, do NOT push to GitHub

## Overview

Add a self-service challenge system where users can create custom milestones with:
- A detailed description of the challenge goal
- A deadline/time limit
- Self-chosen XP and/or mysterious box rewards
- A completion note requirement when claiming

## Background

The existing milestone system (hardcoded `MILESTONES` array in app.html, lines ~2207-2220) provides ~13 fixed milestones (e.g. "4 HOURS IN A DAY", "3 DAY STREAK"). These cannot be modified by the user. The new feature allows users to define their own challenges alongside the built-in ones.

## Data Model

### New Supabase Table: `custom_milestones`

| Column | Type | Default | Description |
|--------|------|---------|-------------|
| `id` | serial PK | auto | Auto-increment ID |
| `title` | text NOT NULL | - | Challenge title (e.g. "一分钟跳绳100个") |
| `description` | text | NULL | Detailed description / rules |
| `xp_reward` | int | 0 | XP awarded on completion |
| `box_reward` | int | 0 | Number of mysterious boxes awarded |
| `deadline` | timestamp NOT NULL | - | Challenge deadline |
| `status` | text | 'active' | `active` / `completed` / `expired` / `abandoned` |
| `completion_note` | text | NULL | Note filled in when claiming |
| `completed_at` | timestamp | NULL | Completion timestamp |
| `created_at` | timestamp | now() | Creation timestamp |

### In-Memory State

```js
S.customMs = []; // loaded from Supabase in loadAll()
```

### CRUD Functions (matching existing style)

```js
async function loadCustomMilestones()
  // SELECT * FROM custom_milestones ORDER BY created_at DESC

async function saveCustomMilestone(m)
  // INSERT INTO custom_milestones (title, description, xp_reward, box_reward, deadline)

async function updateCustomMilestone(id, data)
  // UPDATE custom_milestones SET ... WHERE id = ?

async function deleteCustomMilestone(id)
  // DELETE FROM custom_milestones WHERE id = ?
```

## UI Design

### Entry Point

In the Settings page (`drawSettings()`), below the existing hardcoded MILESTONES section, add a new section: **"🎯 MY CHALLENGES"**

Contains:
- Custom milestone list (active first, then completed/expired)
- "+ CREATE CHALLENGE" button at the top

### Create Challenge Modal

```
┌─────────────────────────┐
│   🎯 NEW CHALLENGE      │
├─────────────────────────┤
│                         │
│  Title:                 │
│  [                   ]  │
│                         │
│  Description:           │
│  [                     ]│
│  [                     ]│
│                         │
│  XP Reward:    [    ]   │
│  Box Reward:   [    ]   │
│                         │
│  Deadline:              │
│  [  3 days ▼  ]         │
│  (1d / 3d / 7d / 14d / │
│   30d / custom)         │
│                         │
│  [CANCEL]   [CREATE 🎯] │
└─────────────────────────┘
```

### Milestone Card (Active)

```
┌─────────────────────────────┐
│ 🎯 Title                     │
│ Description text...          │
│                              │
│ ⏰ Remaining: 2d 14h         │
│ 🏆 +50 XP   +1 Box          │
│                              │
│ [CLAIM ✅]     [ABANDON ✗]  │
└─────────────────────────────┘
```

### Milestone Card (Completed)

```
┌─────────────────────────────┐
│ ✅ Title                     │
│ Description text...          │
│                              │
│ Completed: 2026-08-10        │
│ Note: "跳了120个！"           │
│ 🏆 +50 XP   +1 Box          │
└─────────────────────────────┘
```

### Milestone Card (Expired)

```
┌─────────────────────────────┐
│ ❌ Title (greyed out)        │
│ Description text...          │
│                              │
│ EXPIRED                      │
│ 🏆 +50 XP   +1 Box          │
│                              │
│ [DELETE 🗑]                   │
└─────────────────────────────┘
```

### CLAIM Flow

1. User taps "CLAIM ✅"
2. A modal appears with a textarea for completion note
3. User fills in note (required, min 1 char)
4. User taps "CONFIRM"
5. System:
   - Updates milestone status to `completed`
   - Saves completion_note and completed_at
   - Calls `addXP(xp_reward)` for XP
   - Creates `box_reward` number of "MYSTERIOUS BOX" loot entries via `saveLoot()`
   - Shows celebration HUD with `hud()` + sound effect
   - Refreshes display

## Lifecycle

```
CREATE → ACTIVE ─→ COMPLETED  (user claims with note)
                ─→ EXPIRED    (deadline passes, auto-marked)
                ─→ ABANDONED  (user gives up)
```

### Expiration Check

- On every `loadAll()`, after loading custom milestones:
  - For each `status === 'active'` entry where `deadline < now()`:
    - Update status to `'expired'` in Supabase
    - Update in-memory state
- Expired milestones are kept for history, shown greyed out

### Abandon

- User taps "ABANDON ✗" on an active milestone
- Confirmation dialog: "Are you sure you want to abandon this challenge?"
- On confirm: update status to `'abandoned'`
- No rewards given

## Integration with Existing Systems

### XP System
- Claim calls existing `addXP(amount)` function
- Triggers existing `updXP()` to refresh the XP display
- Counts toward total XP stats

### Mysterious Box (Loot) System
- Each box reward creates a new loot entry via `saveLoot()`:
  ```js
  { nm: "MYSTERIOUS BOX", cn: "神秘宝箱", cost: 0, status: "UNSPENT", ts: Date.now() }
  ```
- Boxes appear in inventory and can be opened like any other box

### Settings Page Layout
- **Top section**: Existing hardcoded MILESTONES (unchanged)
- **Bottom section**: "🎯 MY CHALLENGES"
  - Active milestones (sorted by deadline, earliest first)
  - Collapsed "Completed / Expired" section (tap to expand)
  - "+ CREATE CHALLENGE" button

### Notifications
- Claim success: existing `hud()` celebration + `snd()` sound effect
- Expiration: no notification (passive, shown greyed out on next load)

### Data Loading
- `loadAll()` gains one additional query:
  ```js
  var {data: cm} = await sb.from('custom_milestones').select().order('created_at', {ascending: false});
  S.customMs = cm || [];
  ```

### Reset
- `resetData()` adds:
  ```js
  await sb.from('custom_milestones').delete().neq('id', 0);
  S.customMs = [];
  ```

## Supabase Schema (SQL)

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
```

## Out of Scope

- Android/Kotlin version (separate feature)
- Milestone templates / sharing between users
- Recurring milestones
- Automatic condition checking (all milestones are self-reported)
- Milestone categories or tags

## Testing Plan

1. Create a milestone with various deadline options
2. Verify countdown display updates correctly
3. Claim a milestone, verify XP and boxes are awarded
4. Verify completion note is saved and displayed
5. Let a milestone expire, verify it shows as expired
6. Abandon a milestone, verify no rewards given
7. Verify data persists across page reloads
8. Verify reset clears custom milestones
9. Test edge cases: 0 XP + 0 boxes, very long deadline, past deadline creation
