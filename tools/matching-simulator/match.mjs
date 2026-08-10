const policy = {
  route: .30, schedule: .20, preference: .15, social: .10,
  history: .10, parking: .10, reliability: .05
};
const candidates = [
  { id:'Alex', route:98, schedule:96, preference:78, social:84, history:90, parking:90, reliability:95 },
  { id:'Sam', route:92, schedule:94, preference:94, social:93, history:98, parking:70, reliability:91 },
  { id:'Jordan', route:100, schedule:80, preference:70, social:65, history:50, parking:100, reliability:88 }
];
const score = c => Object.entries(policy).reduce((s,[k,w]) => s + c[k]*w, 0);
const ranked = candidates.map(c => ({...c, score:+score(c).toFixed(2)})).sort((a,b)=>b.score-a.score);
console.table(ranked.map(({id,score})=>({candidate:id,score})));
