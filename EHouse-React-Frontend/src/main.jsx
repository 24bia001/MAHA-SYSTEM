import React, {useEffect, useMemo, useState} from "react";
import {createRoot} from "react-dom/client";
import "./styles.css";
import mahaLogo from "./assets/maha-logo.png";

const API = (import.meta.env.VITE_API_URL || "/api").replace(/\/$/, "");

async function api(path, options={}) {
  const token = sessionStorage.getItem("ehouse_token");
  const headers = {"Content-Type":"application/json", ...(options.headers||{})};
  if(token) headers.Authorization = `Bearer ${token}`;

  let res;
  try {
    res = await fetch(`${API}${path}`, {...options, headers});
  } catch (networkError) {
    throw new Error(
      `Cannot connect to the E-House backend. Make sure Spring Boot is running on http://localhost:8081. ` +
      `If you changed the backend port, set VITE_API_URL in the frontend .env file.`
    );
  }

  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = text; }
  if(!res.ok){
    if(res.status===403){
      const user=JSON.parse(sessionStorage.getItem("ehouse_user")||"null");
      const expected=path.startsWith("/customer/")?"CUSTOMER":path.startsWith("/payments")||path.startsWith("/bookings")||path.startsWith("/houses")||path.startsWith("/sales")?"SELLER":null;
      if(expected && user?.role && user.role!==expected){
        throw new Error(`Your ${user.role.toLowerCase()} session cannot access this ${expected.toLowerCase()} action. Please sign out and sign in as ${expected.toLowerCase()}.`);
      }
      throw new Error("Access denied (403). Please sign out and sign in again so the correct portal permissions are loaded.");
    }
    throw new Error(data?.message || data?.error || text || `Request failed (${res.status})`);
  }
  return data;
}
const images = [
  "https://images.unsplash.com/photo-1600607687920-4e2a09cf159d?auto=format&fit=crop&w=1200&q=85",
  "https://images.unsplash.com/photo-1600566753190-17f0baa2a6c3?auto=format&fit=crop&w=1200&q=85",
  "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=1200&q=85",
  "https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=1200&q=85",
  "https://images.unsplash.com/photo-1600047509807-ba8f99d2cdde?auto=format&fit=crop&w=1200&q=85",
  "https://images.unsplash.com/photo-1600566753086-00f18fb6b3ea?auto=format&fit=crop&w=1200&q=85"
];

function statusOf(value){ return String(value||"").trim().toUpperCase(); }
function money(n){ return new Intl.NumberFormat("en-TZ",{style:"currency",currency:"TZS",maximumFractionDigits:0}).format(Number(n||0)); }
function initials(name="User"){ return name.split(" ").map(x=>x[0]).slice(0,2).join("").toUpperCase(); }
function readImage(file,setter){
  if(!file) return;
  if(file.size > 2 * 1024 * 1024){ alert("Please choose an image smaller than 2 MB."); return; }
  const reader=new FileReader();
  reader.onload=()=>setter(reader.result);
  reader.readAsDataURL(file);
}


function printReceiptReport(p){
  const w=window.open("","_blank","width=900,height=900");
  if(!w) return;
  const status=statusOf(p.status)==="PAID"?"PAYMENT RECEIVED":"AWAITING RECEIPT";
  const total=money(p.totalPaid), remaining=money(p.remainingAmount);
  w.document.write(`<!doctype html><html><head><title>MAHA E-HOUSING Receipt #${p.paymentId||""}</title><style>
  @page{size:A4;margin:14mm}*{box-sizing:border-box}body{font-family:Arial,sans-serif;color:#102019;margin:0;padding:28px;position:relative;background:#fff}
  body:before{content:"MAHA E-HOUSING";position:fixed;inset:35% 0 auto 0;text-align:center;font-size:62px;letter-spacing:8px;color:#0a4d3810;transform:rotate(-25deg);font-weight:900;z-index:-1}
  .head{display:flex;gap:18px;align-items:center;border-bottom:3px solid #c7a56a;padding-bottom:18px}.logo{width:78px;height:78px;object-fit:contain}.brand{font-size:25px;font-weight:900}.sub{color:#6f7d76;font-size:11px;margin-top:5px}.badge{margin-left:auto;border:1px solid #b9ddc7;background:#edf8f1;color:#1f7650;padding:9px 14px;border-radius:999px;font-size:10px;font-weight:800;letter-spacing:1px}
  .title{font-size:26px;margin:28px 0 8px}.intro{color:#65736d;font-size:12px}.grid{display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-top:22px}.card{border:1px solid #dfe5df;border-radius:10px;padding:16px}.card h3{margin:0 0 12px;font-size:13px}.row{display:flex;justify-content:space-between;gap:15px;padding:8px 0;border-bottom:1px solid #edf0ec;font-size:11px}.row:last-child{border-bottom:0}.row span{color:#718078}.amount{font-size:19px;font-weight:900;color:#075c40}.balance{font-size:16px;font-weight:800}.footer{margin-top:28px;border-top:1px solid #ddd;padding-top:14px;font-size:9px;color:#75827c}.sign{display:grid;grid-template-columns:1fr 1fr;gap:35px;margin-top:45px}.sig{padding-top:28px;border-top:1px solid #25352e;font-size:10px}.sig b{display:block;font-size:12px;margin-bottom:4px}.print{margin-top:25px;background:#071711;color:white;border:0;padding:12px 18px;border-radius:7px;font-weight:800}
  </style></head><body><div class="head"><img class="logo" src="${mahaLogo}"/><div><div class="brand">MAHA E-HOUSING</div><div class="sub">Official Payment Receipt · Your Home, Your Priority</div></div><div class="badge">${status}</div></div>
  <h1 class="title">Payment Receipt #${p.paymentId||"—"}</h1><p class="intro">This receipt confirms the payment recorded for the property below.</p>
  <div class="grid"><div class="card"><h3>PAYMENT</h3><div class="row"><span>Receipt number</span><b>#${p.paymentId||"—"}</b></div><div class="row"><span>Booking number</span><b>#${p.bookingId||"—"}</b></div><div class="row"><span>Payment date</span><b>${p.paymentDate||"—"}</b></div><div class="row"><span>Payment method</span><b>${p.paymentMethod||"—"}</b></div><div class="row"><span>Payment received</span><b class="amount">${money(p.amount)}</b></div></div>
  <div class="card"><h3>PROPERTY</h3><div class="row"><span>House</span><b>${p.houseTitle||"—"}</b></div><div class="row"><span>Location</span><b>${p.houseLocation||"—"}</b></div><div class="row"><span>House price</span><b>${money(p.housePrice)}</b></div><div class="row"><span>Rooms</span><b>${p.bedrooms??"—"} beds · ${p.bathrooms??"—"} baths · ${p.halls??"—"} halls · ${p.kitchens??"—"} kitchens</b></div><div class="row"><span>Total paid</span><b>${total}</b></div><div class="row"><span>Remaining</span><b class="balance">${remaining}</b></div><div class="row"><span>House status</span><b>${p.houseStatus||"—"}</b></div></div></div>
  <div class="grid"><div class="card"><h3>CUSTOMER</h3><div class="row"><span>Name</span><b>${p.customerName||"—"}</b></div><div class="row"><span>Email</span><b>${p.customerEmail||"—"}</b></div><div class="row"><span>Phone</span><b>${p.customerPhone||"—"}</b></div><div class="row"><span>Address</span><b>${p.customerAddress||"—"}</b></div><div class="row"><span>NIDA</span><b>${p.customerNida||"—"}</b></div></div>
  <div class="card"><h3>SELLER</h3><div class="row"><span>Name</span><b>${p.sellerName||"—"}</b></div><div class="row"><span>Email</span><b>${p.sellerEmail||"—"}</b></div><div class="row"><span>Phone</span><b>${p.sellerPhone||"—"}</b></div><div class="row"><span>Address</span><b>${p.sellerAddress||"—"}</b></div><div class="row"><span>NIDA</span><b>${p.sellerNida||"—"}</b></div></div></div>
  <div class="sign"><div class="sig"><b>Seller signature</b>${p.sellerName||"Seller"}<br/>Date: __________________</div><div class="sig"><b>Customer signature</b>${p.customerName||"Customer"}<br/>Date: __________________</div></div>
  <div class="footer">MAHA E-HOUSING · This document was generated from the E-House portal. Keep it with your property records.</div><script>window.print()</script></body></html>`);w.document.close();
}

function printContractReport(p){
  const w=window.open("","_blank","width=950,height=950");
  if(!w) return;
  const fullyPaid=Number(p.remainingAmount||0)<=0;
  const contractStatus=fullyPaid?"PAID IN FULL / SALE COMPLETED":"INSTALLMENT AGREEMENT / BALANCE OUTSTANDING";
  w.document.write(`<!doctype html><html><head><title>MAHA E-HOUSING Property Contract</title><style>
  @page{size:A4;margin:13mm}*{box-sizing:border-box}body{font-family:Georgia,"Times New Roman",serif;color:#17231e;margin:0;padding:25px;position:relative;line-height:1.45}
  body:before{content:"MAHA E-HOUSING";position:fixed;inset:40% 0 auto 0;text-align:center;font-family:Arial,sans-serif;font-size:55px;letter-spacing:7px;color:#0a4d3810;transform:rotate(-28deg);font-weight:900;z-index:-1}
  .head{display:flex;align-items:center;gap:16px;border-bottom:3px double #b99b61;padding-bottom:14px}.logo{width:82px;height:82px;object-fit:contain}.brand{font-family:Arial,sans-serif;font-size:25px;font-weight:900;letter-spacing:.4px}.sub{font-family:Arial,sans-serif;color:#6c7973;font-size:10px}.status{margin-left:auto;font-family:Arial,sans-serif;font-size:9px;font-weight:900;letter-spacing:.8px;border:1px solid #c7a56a;padding:8px 10px;border-radius:5px;text-align:center;max-width:180px}.title{text-align:center;font-size:23px;margin:25px 0 5px;text-transform:uppercase}.number{text-align:center;font-family:Arial,sans-serif;color:#66746e;font-size:10px}.section{margin-top:20px;border:1px solid #d9dfda;border-radius:8px;padding:14px}.section h3{font-family:Arial,sans-serif;font-size:11px;letter-spacing:1px;margin:0 0 10px;color:#0a5a3f}.grid{display:grid;grid-template-columns:1fr 1fr;gap:12px}.field{font-size:10px;border-bottom:1px solid #edf0ed;padding:5px 0}.field span{color:#718078;display:inline-block;min-width:105px}.terms{font-size:10px;margin:10px 0}.terms li{margin:6px 0}.schedule{width:100%;border-collapse:collapse;font-family:Arial,sans-serif;font-size:9px;margin-top:10px}.schedule th{background:#071711;color:#fff;text-align:left;padding:8px}.schedule td{border-bottom:1px solid #e0e4df;padding:7px}.signatures{display:grid;grid-template-columns:1fr 1fr;gap:35px;margin-top:36px}.signature{padding-top:35px;border-top:1px solid #25352e;font-family:Arial,sans-serif;font-size:9px}.signature strong{display:block;font-size:11px;margin-bottom:3px}.note{font-family:Arial,sans-serif;font-size:8px;color:#69766f;margin-top:20px;border-top:1px solid #ddd;padding-top:9px}.footer{font-family:Arial,sans-serif;text-align:center;font-size:8px;color:#78837e;margin-top:16px}
  </style></head><body><div class="head"><img class="logo" src="${mahaLogo}"/><div><div class="brand">MAHA E-HOUSING</div><div class="sub">PROPERTY PURCHASE & PAYMENT AGREEMENT</div></div><div class="status">${contractStatus}</div></div>
  <h1 class="title">Property Purchase Contract</h1><div class="number">Booking #${p.bookingId||"—"} · Payment/record #${p.paymentId||"—"} · ${p.paymentDate||new Date().toISOString().slice(0,10)}</div>
  <div class="section"><h3>1. PROPERTY DETAILS</h3><div class="grid"><div><div class="field"><span>House:</span><b>${p.houseTitle||"—"}</b></div><div class="field"><span>Location:</span><b>${p.houseLocation||"—"}</b></div><div class="field"><span>Bedrooms:</span><b>${p.bedrooms??"—"}</b></div><div class="field"><span>Bathrooms:</span><b>${p.bathrooms??"—"}</b></div><div class="field"><span>Halls / Kitchens:</span><b>${p.halls??"—"} / ${p.kitchens??"—"}</b></div></div><div><div class="field"><span>Price:</span><b>${money(p.housePrice)}</b></div><div class="field"><span>House status:</span><b>${p.houseStatus||"—"}</b></div><div class="field"><span>Booking date:</span><b>${p.bookingDate||"—"}</b></div><div class="field"><span>Booking time:</span><b>${p.bookingTime||"—"}</b></div></div></div><p class="terms"><b>Description:</b> ${p.houseDescription||"—"}</p></div>
  <div class="section"><h3>2. SELLER DETAILS</h3><div class="grid"><div><div class="field"><span>Full name:</span><b>${p.sellerName||"—"}</b></div><div class="field"><span>Email:</span><b>${p.sellerEmail||"—"}</b></div><div class="field"><span>Phone:</span><b>${p.sellerPhone||"—"}</b></div></div><div><div class="field"><span>Address:</span><b>${p.sellerAddress||"—"}</b></div><div class="field"><span>NIDA:</span><b>${p.sellerNida||"—"}</b></div><div class="field"><span>Seller ID:</span><b>#${p.sellerId||"—"}</b></div></div></div></div>
  <div class="section"><h3>3. CUSTOMER / BUYER DETAILS</h3><div class="grid"><div><div class="field"><span>Full name:</span><b>${p.customerName||"—"}</b></div><div class="field"><span>Email:</span><b>${p.customerEmail||"—"}</b></div><div class="field"><span>Phone:</span><b>${p.customerPhone||"—"}</b></div></div><div><div class="field"><span>Address:</span><b>${p.customerAddress||"—"}</b></div><div class="field"><span>NIDA:</span><b>${p.customerNida||"—"}</b></div><div class="field"><span>Customer ID:</span><b>#${p.customerId||"—"}</b></div></div></div></div>
  <div class="section"><h3>4. PAYMENT TERMS & RECORD</h3><table class="schedule"><thead><tr><th>House price</th><th>This payment</th><th>Total paid</th><th>Remaining balance</th><th>Method</th><th>Status</th></tr></thead><tbody><tr><td>${money(p.housePrice)}</td><td>${money(p.amount)}</td><td>${money(p.totalPaid)}</td><td>${money(p.remainingAmount)}</td><td>${p.paymentMethod||"—"}</td><td>${statusOf(p.status)}</td></tr></tbody></table><ul class="terms"><li>The seller confirms receipt of the recorded payment when the payment status is marked RECEIVED.</li><li>The remaining balance is the house price less all seller-confirmed payments recorded against this booking.</li><li>The property is reserved as SOLD OUT after a seller-confirmed payment, while any outstanding balance remains payable under the agreed installment arrangement.</li><li>Final sale completion is recorded when the total confirmed payments reach the full house price.</li><li>This document records the transaction between the parties and does not replace any legally required transfer, registration, tax, or governmental documentation.</li></ul></div>
  <div class="signatures"><div class="signature"><strong>SELLER SIGNATURE</strong>${p.sellerName||"Seller"}<br/>Signature: ______________________________<br/>Date: __________________</div><div class="signature"><strong>CUSTOMER / BUYER SIGNATURE</strong>${p.customerName||"Customer"}<br/>Signature: ______________________________<br/>Date: __________________</div></div>
  <div class="note"><b>Company record:</b> MAHA E-HOUSING · E-House Property Selling System · Keep this signed document with the official receipt and supporting identification records.</div><div class="footer">MAHA E-HOUSING · Your Home, Your Priority · Official system-generated contract report</div><script>window.print()</script></body></html>`);w.document.close();
}

function App(){
  const [page,setPage]=useState("home");
  const [houses,setHouses]=useState([]);
  const [loading,setLoading]=useState(true);
  const [query,setQuery]=useState("");
  const [selected,setSelected]=useState(null);
  const [auth,setAuth]=useState(()=>JSON.parse(sessionStorage.getItem("ehouse_user")||"null"));
  const [notice,setNotice]=useState("");
  const [authMode,setAuthMode]=useState("login");
  const [authRole,setAuthRole]=useState("customer");

  const loadHouses=async()=>{
    setLoading(true);
    try {
      const data = await api(auth?.role==="CUSTOMER" ? "/customer/houses" : "/houses");
      setHouses(Array.isArray(data)?data:[]);
    } catch(e) { setNotice(e.message); }
    finally { setLoading(false); }
  };
  useEffect(()=>{ if(page==="home" || page==="dashboard" || page==="seller") loadHouses(); },[page,auth?.role]);

  const filtered=useMemo(()=>houses.filter(h=>
    `${h.title} ${h.location} ${h.description}`.toLowerCase().includes(query.toLowerCase())
  ),[houses,query]);

  function logout(){
    sessionStorage.removeItem("ehouse_token"); sessionStorage.removeItem("ehouse_user");
    localStorage.removeItem("ehouse_token"); localStorage.removeItem("ehouse_user");
    setAuth(null); setPage("home"); setNotice("You have been signed out.");
  }
  function go(p){setPage(p); window.scrollTo({top:0,behavior:"smooth"});}

  return <div className="app">
    {(!auth || page==="home" || page==="about") && <header className="nav publicNav">
      <button type="button" className="brand" onClick={()=>go("home")}><span className="brandLogo"><img src={mahaLogo} alt="MAHA E-HOUSING"/></span><span>E-House</span></button>
      <nav>
        <button className={page==="home"?"active":""} onClick={()=>go("home")}>Explore</button>
        <button onClick={()=>go("about")}>How it works</button>
      </nav>
      <div className="navRight">
        <button type="button" className="outlineBtn" onClick={()=>{setAuthMode("login");go("auth")}}>Sign in</button>
        <button className="primaryBtn" onClick={()=>{setAuthMode("register");setAuthRole("customer");go("auth")}}>Join E-House</button>
      </div>
    </header>}

    {notice && <div className="toast"><span>●</span>{notice}<button onClick={()=>setNotice("")}>×</button></div>}

    {page==="home" && <Home houses={filtered} loading={loading} query={query} setQuery={setQuery} openHouse={h=>{setSelected(h);go("details")}} go={go}/>}
    {page==="details" && <Details house={selected} auth={auth} go={go} setNotice={setNotice}/>}
    {page==="about" && <About go={go}/>}
    {page==="auth" && <Auth mode={authMode} role={authRole} setRole={setAuthRole} setMode={setAuthMode} onAuth={u=>{setAuth(u);go(u.role==="CUSTOMER"?"dashboard":u.role==="SELLER"?"seller":"admin")}} setNotice={setNotice}/>}
    {page==="dashboard" && <CustomerDashboard auth={auth} houses={houses} go={go} setNotice={setNotice} setSelected={setSelected} logout={logout}/>}
    {page==="seller" && <SellerDesk auth={auth} go={go} setNotice={setNotice} logout={logout}/>}
    {page==="admin" && <AdminDesk auth={auth} setNotice={setNotice} go={go} logout={logout}/>}
    <Footer auth={auth} go={go}/>
  </div>
}

function Footer({auth,go}){
 return <footer className={`siteFooter ${auth?"dashboardFooter":""}`}>
   <div className="footerBottom">
     <span>© {new Date().getFullYear()} MAHA E-HOUSING. All rights reserved.</span>
     <span>MAHA E-HOUSING — digital property selling.</span>
   </div>
 </footer>
}

function Home({houses,loading,query,setQuery,openHouse,go}){
 return <main>
  <section className="hero">
    <div className="heroGlow"></div>
    <div className="heroCopy">
      <div className="eyebrow">REAL ESTATE, REIMAGINED</div>
      <h1>Find a place<br/><i>worth coming home to.</i></h1>
      <p>Discover verified homes, book a viewing and keep every step of your purchase in one calm, simple space.</p>
      <div className="searchBox"><span>⌕</span><input value={query} onChange={e=>setQuery(e.target.value)} placeholder="Search by location, house name or feature..." /><kbd>⌘ K</kbd></div>
      <div className="heroStats"><span><b>01</b> Explore</span><span><b>02</b> Book</span><span><b>03</b> Own</span></div>
    </div>
    <div className="heroVisual"><img src={images[0]}/><div className="floatingCard"><span className="dot"></span><div><b>Curated homes</b><small>Made for real life</small></div><strong>→</strong></div></div>
  </section>

  <section className="section">
   <div className="sectionHead"><div><span className="eyebrow">THE COLLECTION</span><h2>Homes with a story.</h2></div><button type="button" className="textBtn" onClick={()=>setQuery("")}>Clear search ↗</button></div>
   {loading ? <div className="loader">Loading homes<span>•••</span></div> :
    houses.length ? <div className="grid">{houses.map((h,i)=><HouseCard key={h.houseId||i} h={h} i={i} onClick={()=>openHouse(h)}/>)}</div> :
    <div className="empty"><div>⌂</div><h3>No homes found</h3><p>Try another location or search phrase.</p></div>}
  </section>

  <section className="manifesto">
    <div><span className="eyebrow">WHY E-HOUSE</span><h2>Less paperwork.<br/><i>More certainty.</i></h2></div>
    <div className="principles"><article><b>01</b><h3>One place</h3><p>Bookings, payments, sales and documents stay connected to your property journey.</p></article><article><b>02</b><h3>Human support</h3><p>See seller details and keep the conversation close to the home you want.</p></article><article><b>03</b><h3>Clear progress</h3><p>Your dashboard turns a complicated purchase into simple next steps.</p></article></div>
  </section>
  <section className="cta"><div><span className="eyebrow">READY WHEN YOU ARE</span><h2>Your next address<br/>could be here.</h2></div><button className="lightBtn" onClick={()=>go("auth")}>Create your account →</button></section>
 </main>
}

function HouseCard({h,i,onClick}){
 const soldOut=statusOf(h.status)==="SOLD_OUT";
 return <article className={`houseCard ${soldOut?"soldOutHouse":""}`} onClick={onClick}>
   <div className="imageWrap"><img src={h.image||images[i%images.length]}/><span className={`tag ${soldOut?"soldOutTag":""}`}>{soldOut?"SOLD OUT":"AVAILABLE"}</span><button onClick={e=>{e.stopPropagation();onClick()}}>↗</button></div>
   <div className="cardBody"><div><span className="muted">{h.location}</span><h3>{h.title}</h3></div><strong>{money(h.price)}</strong></div>
   <div className="features"><span>⌂ {h.bedrooms} beds</span><span>◈ {h.bathrooms} baths</span><span>▱ {h.halls??0} halls</span><span>▣ {h.kitchens??0} kitchens</span><span>#{h.houseId}</span></div>
 </article>
}

function Details({house,auth,go,setNotice}){
 if(!house) return <div className="empty pagePad"><h2>Select a house first.</h2><button className="primaryBtn" onClick={()=>go("home")}>Back to homes</button></div>;
 const backPage = auth?.role === "SELLER" ? "seller" : auth?.role === "ADMIN" ? "admin" : auth?.role === "CUSTOMER" ? "dashboard" : "home";
 const backLabel = auth?.role === "SELLER" ? "Back to seller dashboard" : auth?.role === "ADMIN" ? "Back to admin dashboard" : auth?.role === "CUSTOMER" ? "Back to customer dashboard" : "Back to collection";
 const [date,setDate]=useState("");
 const [time,setTime]=useState("");
 const [busy,setBusy]=useState(false);
 const [success,setSuccess]=useState(null);
 const today=new Date().toISOString().slice(0,10);

 async function book(){
   if(!auth){go("auth");return}
   if(auth.role!=="CUSTOMER"){
     setNotice("Please sign in with a customer account to book a private viewing.");
     return;
   }
   if(!date || !time){
     setNotice("Choose both a viewing date and time.");
     return;
   }
   setBusy(true);
   try {
     const booking=await api("/customer/bookings",{
       method:"POST",
       body:JSON.stringify({bookingDate:date,bookingTime:time,houseId:house.houseId})
     });
     setSuccess(booking||{bookingDate:date,bookingTime:time});
   } catch(e){
     setNotice(e.message || "We could not create the booking. Please try again.");
   } finally { setBusy(false); }
 }

 return <main className="pagePad detailsPage">
  <button type="button" className="backBtn" onClick={()=>go(backPage)}>← {backLabel}</button>
  <div className="detail">
   <div className="detailImage">
    <img src={house.image||images[(house.houseId||1)%images.length]}/>
    <span>HOUSE #{house.houseId}</span>
   </div>
   <div className="detailInfo">
    <span className="eyebrow">{house.location}</span>
    <h1>{house.title}</h1>
    <div className="bigPrice">{money(house.price)}</div>
    <div className="featureRow">
      <b>{house.bedrooms}<small>Bedrooms</small></b>
      <b>{house.bathrooms}<small>Bathrooms</small></b>
      <b>{house.halls??0}<small>Halls</small></b>
      <b>{house.kitchens??0}<small>Kitchens</small></b>
    </div>
    <p>{house.description}</p>

    <div className="sellerProfileCard">
      <div className="sellerAvatarWrap">
        {house.sellerImage ? <img src={house.sellerImage} alt={house.sellerName||"Seller"}/> : <span>{initials(house.sellerName||"Seller")}</span>}
      </div>
      <div className="sellerProfileText">
        <span className="eyebrow">LISTED BY</span>
        <strong>{house.sellerName||"Verified seller"}</strong>
        {house.sellerPhone && <small>{house.sellerPhone}</small>}
        {house.sellerEmail && <small>{house.sellerEmail}</small>}
      </div>
      <span className="verifiedPill">✓ VERIFIED</span>
    </div>

    <div className="bookingPanel premiumBookingPanel">
      <div className="bookingHeading">
        <div>
          <span className="bookingIcon">◷</span>
          <b>Book a private viewing</b>
          <small>Select the day and time you want to visit this home.</small>
        </div>
        <span className="secureLabel">SECURE REQUEST</span>
      </div>
      <div className="bookingFields">
        <label><span>Date</span><input type="date" min={today} value={date} onChange={e=>setDate(e.target.value)}/></label>
        <label><span>Time</span><input type="time" min="08:00" max="18:00" step="1800" value={time} onChange={e=>setTime(e.target.value)}/></label>
      </div>
      <div className="bookingSummary">
        <span>Viewing request</span>
        <b>{date ? new Date(`${date}T00:00:00`).toLocaleDateString("en-GB",{day:"2-digit",month:"short",year:"numeric"}) : "Choose a date"}{time ? ` · ${time}` : " · Choose a time"}</b>
      </div>
      <button className="primaryBtn bookingSubmit" disabled={busy || !date || !time} onClick={book}>
        {busy ? "Sending request…" : "Request private viewing →"}
      </button>
      <small className="bookingHint">Your request will appear in your bookings and be sent to the seller for approval.</small>
    </div>
   </div>
  </div>

  {success && <div className="successOverlay" role="dialog" aria-modal="true">
    <div className="successCard">
      <div className="successMark">✓</div>
      <span className="eyebrow">BOOKING RECEIVED</span>
      <h2>Congratulations!</h2>
      <p>Your private viewing for <strong>{house.title}</strong> has been booked successfully.</p>
      <div className="successDetails">
        <div><small>HOUSE</small><b>{house.title}</b></div>
        <div><small>DATE</small><b>{success.bookingDate||date}</b></div>
        <div><small>TIME</small><b>{success.bookingTime||time}</b></div>
        <div><small>SELLER</small><b>{house.sellerName||"Verified seller"}</b></div>
      </div>
      <div className="successActions">
        <button className="primaryBtn" onClick={()=>{setSuccess(null);go("dashboard")}}>View my bookings →</button>
        <button className="outlineBtn" onClick={()=>setSuccess(null)}>Stay on this house</button>
      </div>
    </div>
  </div>}
 </main>
}
function About({go}){return <main className="pagePad about"><span className="eyebrow">HOW E-HOUSE WORKS</span><h1>A quieter way to<br/><i>buy property.</i></h1><div className="steps"><div><b>01</b><h2>Discover</h2><p>Browse the available homes coming from your E-House backend.</p></div><div><b>02</b><h2>Connect</h2><p>Create an account and request a viewing for the home you love.</p></div><div><b>03</b><h2>Move forward</h2><p>Track bookings, payments, sales and documents from your portal.</p></div></div><button className="primaryBtn" onClick={()=>go("home")}>Explore homes →</button></main>}

function Auth({mode,role,setRole,setMode,onAuth,setNotice}){
 const [form,setForm]=useState({name:"",email:"",phone:"",address:"",nida:"",password:""});
 const [busy,setBusy]=useState(false);

 async function submit(e){
   e.preventDefault();
   setBusy(true);
   try{
     if(mode==="register"){
       if(role!=="customer") throw new Error("Only customers can register themselves. Please contact the administrator for a seller account.");
       const data=await api("/auth/customer/register",{method:"POST",body:JSON.stringify({
         name:form.name,email:form.email,phone:form.phone,address:form.address,nida:form.nida,password:form.password,image:form.image||null
       })});
       setNotice("Customer account created. Please sign in.");
       setMode("login");
       return;
     }

     const endpoint=role==="customer"
       ? "/auth/customer/login"
       : role==="seller"
         ? "/auth/seller/login"
         : "/auth/management/login";

     sessionStorage.removeItem("ehouse_token");
     sessionStorage.removeItem("ehouse_user");
     localStorage.removeItem("ehouse_token");
     localStorage.removeItem("ehouse_user");
     const data=await api(endpoint,{method:"POST",body:JSON.stringify({
       email:form.email,password:form.password
     })});

     const r=role==="customer"?"CUSTOMER":role==="seller"?"SELLER":"ADMIN";
     const user={...data,role:r};
     sessionStorage.setItem("ehouse_token",data.token);
     sessionStorage.setItem("ehouse_user",JSON.stringify(user));
     onAuth(user);
   }catch(e){setNotice(e.message)}
   finally{setBusy(false)}
 }

 return <main className="authPage">
   <div className="authVisual"><span className="brandMark">EH</span><h1>Property should feel<br/><i>personal.</i></h1><p>A focused portal for finding, booking and owning your next home.</p></div>
   <div className="authPanel">
     <span className="eyebrow">{mode==="login"?"WELCOME BACK":"CUSTOMER REGISTRATION"}</span>
     <h2>{mode==="login"?"Sign in":"Create customer account"}</h2>

     {mode==="login" ? <div className="roleTabs">
       {["customer","seller","admin"].map(r=><button key={r} className={role===r?"selected":""} onClick={()=>setRole(r)}>{r}</button>)}
     </div> : <div className="noticeBox">Customer registration is open. Seller accounts are created by an administrator.</div>}

     <form onSubmit={submit}>
       {mode==="register" && <><input placeholder="Full name" required value={form.name} onChange={e=>setForm({...form,name:e.target.value})}/>
         <div className="two"><input placeholder="Phone" required value={form.phone} onChange={e=>setForm({...form,phone:e.target.value})}/><input placeholder="NIDA" required value={form.nida} onChange={e=>setForm({...form,nida:e.target.value})}/></div><input placeholder="Address" required value={form.address} onChange={e=>setForm({...form,address:e.target.value})}/>
         <label className="fileField">Profile picture<input type="file" accept="image/*" onChange={e=>readImage(e.target.files?.[0],image=>setForm({...form,image}))}/></label>
       </>}
       <input type="email" placeholder="Email address" required value={form.email} onChange={e=>setForm({...form,email:e.target.value})}/>
       <input type="password" placeholder="Password" minLength="6" required value={form.password} onChange={e=>setForm({...form,password:e.target.value})}/>
       <button className="primaryBtn wide" disabled={busy}>{busy?"Please wait…":mode==="login"?"Sign in →":"Create account →"}</button>
     </form>
     <p className="switch">{mode==="login"?"New customer?":"Already have an account?"} <button onClick={()=>{setMode(mode==="login"?"register":"login");setRole("customer")}}>{mode==="login"?"Register":"Sign in"}</button></p>
   </div>
 </main>
}


function DashboardTopbar({auth, role}){
 return <div className="dashboardTopbar">
   <div className="dashboardTopbarBrand"><span className="dashboardLogo"><img src={mahaLogo} alt="MAHA E-HOUSING"/></span><div><b>E-House</b><small>{role} Portal</small></div></div>
   <div className="dashboardTopbarProfile">
     <span className="topbarStatus"></span>
     <span className="topbarAvatar">{auth?.image?<img src={auth.image} alt=""/>:<span>{initials(auth?.name||"User")}</span>}</span>
     <span className="topbarIdentity"><b>{auth?.name||"User"}</b><small>{role}</small></span>
   </div>
 </div>
}

function DashboardSidebar({role, name, image, tab, setTab, items, go, logout, actionLabel, onAction}){
 return <aside className="luxSidebar">
   <div className="sideBrand"><span className="sideLogo"><img src={mahaLogo} alt="MAHA E-HOUSING"/></span><div><b>MAHA E-HOUSING</b><small>{role} PORTAL</small></div></div>
   <div className="sideUser">
     <div className="sideAvatar">{image?<img src={image} alt=""/>:initials(name)}</div>
     <div><b>{name||role}</b><small>Signed in</small></div>
   </div>
   <div className="sideLabel">WORKSPACE</div>
   <nav className="sideNav">{items.map(item=><button key={item.id} className={tab===item.id?"active":""} onClick={()=>setTab(item.id)}><span>{item.icon}</span><span>{item.label}</span>{tab===item.id&&<i>•</i>}</button>)}</nav>
   <div className="sideBottom">
     {actionLabel&&<button className="sideAction" onClick={onAction}>{actionLabel} <span>↗</span></button>}
     <button className="sideHome" onClick={logout}>↪ <span>Logout</span></button>
   </div>
 </aside>
}

function CustomerDashboard({auth,houses,go,setNotice,setSelected,logout}){
 const [tab,setTab]=useState("overview");
 const [bookings,setBookings]=useState([]), [payments,setPayments]=useState([]), [sales,setSales]=useState([]), [docs,setDocs]=useState([]);
 const [pay,setPay]=useState({bookingId:"",amount:"",paymentMethod:"Mobile Money"});
 const load=async()=>{
  const results=await Promise.allSettled([api("/customer/bookings"),api("/customer/payments"),api("/customer/sales"),api("/customer/documents")]);
  const setters=[setBookings,setPayments,setSales,setDocs];
  results.forEach((r,i)=>{ if(r.status==="fulfilled") setters[i](Array.isArray(r.value)?r.value:[]); else { setters[i]([]); setNotice(r.reason?.message||"Could not load customer data."); } });
};
 useEffect(()=>{load()},[]);
 async function cancel(id){try{await api(`/customer/bookings/${id}/cancel`,{method:"PUT"});setNotice("Booking cancelled.");load()}catch(e){setNotice(e.message)}}
 function bookingPaymentInfo(b){
   if(!b) return {house:null,price:0,paid:0,remaining:0,pending:false};
   const house=houses.find(h=>Number(h.houseId)===Number(b.houseId));
   const bookingPayments=payments.filter(p=>Number(p.bookingId)===Number(b.bookingId));
   const latest=bookingPayments[bookingPayments.length-1];
   const price=Number(house?.price ?? latest?.housePrice ?? 0);
   const paid=Number(latest?.totalPaid ?? bookingPayments.filter(p=>statusOf(p.status)==="PAID").reduce((a,p)=>a+Number(p.amount||0),0));
   return {house,price,paid,remaining:Math.max(0,price-paid),pending:bookingPayments.some(p=>statusOf(p.status)==="PENDING")};
 }
 async function payBooking(e){e.preventDefault();try{await api("/customer/payments",{method:"POST",body:JSON.stringify({bookingId:Number(pay.bookingId),amount:Number(pay.amount),paymentDate:new Date().toISOString().slice(0,10),paymentMethod:pay.paymentMethod,status:"PENDING"})});setNotice("Payment submitted. The seller will confirm receipt.");setPay({bookingId:"",amount:"",paymentMethod:"Mobile Money"});load()}catch(e){setNotice(e.message)}}
 function receipt(p){printReceiptReport(p)}
 return <main className="dashboardShell"><DashboardSidebar role="CUSTOMER" image={auth?.image} name={auth?.name} tab={tab} setTab={setTab} go={go} logout={logout} actionLabel="Browse houses" onAction={()=>go("home")} items={[{id:"overview",label:"Overview",icon:"◈"},{id:"houses",label:"Discover houses",icon:"⌂"},{id:"bookings",label:"My bookings",icon:"◷"},{id:"payments",label:"Payments & receipts",icon:"◇"},{id:"sales",label:"Purchased homes",icon:"✓"},{id:"documents",label:"Documents",icon:"▣"}]}/><section className="dashboardMain pagePad"><DashboardTopbar auth={auth} role="Customer"/><div className="dashTop"><div><span className="eyebrow">CUSTOMER PORTAL</span><h1>Hello, {auth?.name?.split(" ")[0]||"there"}.</h1><p>Homes, bookings, payments, receipts, purchases and documents.</p></div><button className="primaryBtn" onClick={()=>go("home")}>Browse houses →</button></div>
 <div className="dashContent">{tab==="overview"&&<><div className="metricRow"><div><small>Available houses</small><b>{houses.length}</b></div><div><small>Bookings</small><b>{bookings.length}</b></div><div><small>Payments</small><b>{payments.length}</b></div><div><small>Purchases</small><b>{sales.length}</b></div></div><div className="dashCard"><h3>Next steps</h3><p className="muted">Browse a house, request a viewing, wait for seller confirmation, then pay and keep your receipt here.</p></div></>}
 {tab==="houses"&&<div className="grid">{houses.map((h,i)=><HouseCard key={h.houseId} h={h} i={i} onClick={()=>{setSelected(h);go("details")}}/>)}</div>}
 {tab==="bookings"&&<DataList title="My bookings" data={bookings} cancel={cancel}/>} 
 {tab==="payments"&&<><div className="paymentSuccessBanner"><span>✓</span><div><b>Seller-confirmed payments are official receipts</b><small>Once the seller confirms receipt, the house is reserved as SOLD OUT, the remaining balance is updated, and Receipt + Contract become available.</small></div></div><div className="dashCard"><h3>Make a payment</h3><p className="muted">The full house price is shown automatically. You may pay part of it, and the remaining balance is tracked for you.</p>{!bookings.some(b=>statusOf(b.status)==="CONFIRMED"&&bookingPaymentInfo(b).remaining>0&&!bookingPaymentInfo(b).pending)&&<div className="paymentLocked">No approved booking is ready for a new payment. If you already submitted a payment, wait for the seller to confirm receipt.</div>}<form className="formCard inlineForm" onSubmit={payBooking}><select required value={pay.bookingId} onChange={e=>{const id=e.target.value;const b=bookings.find(x=>String(x.bookingId)===id);const info=b?bookingPaymentInfo(b):null;setPay({...pay,bookingId:id,amount:info?.remaining?String(info.remaining):""})}}><option value="">Select approved booking</option>{bookings.filter(b=>statusOf(b.status)==="CONFIRMED"&&bookingPaymentInfo(b).remaining>0&&!bookingPaymentInfo(b).pending).map(b=>{const info=bookingPaymentInfo(b);return <option key={b.bookingId} value={b.bookingId}>#{b.bookingId} — {b.houseTitle} — {money(info.remaining)} remaining</option>})}</select><input type="number" min="1" max={pay.bookingId?(bookingPaymentInfo(bookings.find(b=>String(b.bookingId)===String(pay.bookingId)))?.remaining||undefined):undefined} required placeholder="Amount (TZS)" value={pay.amount} onChange={e=>setPay({...pay,amount:e.target.value})}/><select value={pay.paymentMethod} onChange={e=>setPay({...pay,paymentMethod:e.target.value})}><option>Mobile Money</option><option>Bank Transfer</option><option>Cash</option></select><button className="primaryBtn" disabled={!pay.bookingId}>Submit payment →</button></form></div><div className="dashCard"><h3>Payment history & balances</h3>{payments.length?payments.map(p=><div className="listRow paymentRow" key={p.paymentId}><span>#{p.paymentId}</span><div><b>{p.houseTitle||`Booking #${p.bookingId}`}</b><small className="paymentSub">House price: {money(p.housePrice)} · Paid: {money(p.totalPaid)} · Remaining: {money(p.remainingAmount)}</small></div><span>{p.paymentDate}</span><em className={`status ${String(p.status).toLowerCase()}`}>{statusOf(p.status)==="PENDING"?"AWAITING RECEIPT":"RECEIVED"}</em><div className="rowActions">{statusOf(p.status)==="PAID"&&<><button className="smallBtn actionReceipt" onClick={()=>receipt(p)}>Receipt</button><button className="smallBtn actionReceipt" onClick={()=>printContractReport(p)}>Contract</button></>}</div></div>) : <p className="muted">No payments yet.</p>}</div></>}
 {tab==="sales"&&<DataList title="Purchased homes" data={sales}/>} {tab==="documents"&&<DataList title="My documents" data={docs}/>}</div></section></main>
}

function DataList({title,data,cancel,receipt}){return <div className="dashCard"><h3>{title}</h3>{data.length?data.map(x=>{const raw=x.status||x.bookingStatus||x.documentStatus||"RECORDED";const label=raw==="CONFIRMED"?"APPROVED":raw;return <div className="listRow" key={x.bookingId||x.paymentId||x.saleId||x.documentId}><span>#{x.bookingId||x.paymentId||x.saleId||x.documentId}</span><b>{x.houseTitle||x.documentType||x.amount&&money(x.amount)||"Record"}</b><span>{x.bookingDate ? `${x.bookingDate}${x.bookingTime ? ` · ${x.bookingTime}` : ""}` : (x.paymentDate||x.saleDate||"")}</span><em className={`status ${String(raw).toLowerCase()}`}>{label}</em>{(cancel&&raw!=="CANCELLED")||receipt?<div className="rowActions">{cancel&&raw!=="CANCELLED"&&<button className="smallBtn actionCancel" onClick={()=>cancel(x.bookingId)}>Cancel</button>}{receipt&&<button className="smallBtn actionReceipt" onClick={()=>receipt(x)}>Receipt</button>}</div>:null}</div>}) : <p className="muted">Nothing here yet.</p>}</div>}

function SellerDesk({auth,go,setNotice,logout}){
 const emptyHouse={title:"",location:"",description:"",price:"",bedrooms:"",bathrooms:"",halls:"",kitchens:"",image:""};
 const [tab,setTab]=useState("overview"),[houses,setHouses]=useState([]),[bookings,setBookings]=useState([]),[payments,setPayments]=useState([]),[sales,setSales]=useState([]);
 const [form,setForm]=useState(emptyHouse),[editing,setEditing]=useState(null),[showAdd,setShowAdd]=useState(false),[pay,setPay]=useState({bookingId:"",amount:"",paymentMethod:"Bank Transfer"});
 const load=async()=>{
  const results=await Promise.allSettled([api("/houses"),api("/bookings"),api("/payments"),api("/sales")]);
  const setters=[setHouses,setBookings,setPayments,setSales];
  const labels=["houses","bookings","payments","sales"];
  results.forEach((r,i)=>{
    if(r.status==="fulfilled") setters[i](Array.isArray(r.value)?r.value:[]);
    else { setters[i]([]); setNotice(r.reason?.message||`Could not load ${labels[i]}.`); }
  });
};
 useEffect(()=>{load()},[]);
 function openAdd(){setEditing(null);setForm(emptyHouse);setShowAdd(true)}
 function openEdit(h){setEditing(h.houseId);setForm({...h,price:h.price,bedrooms:h.bedrooms,bathrooms:h.bathrooms,halls:h.halls??0,kitchens:h.kitchens??0});setShowAdd(true)}
 async function save(e){e.preventDefault();try{const body={...form,price:Number(form.price),bedrooms:Number(form.bedrooms),bathrooms:Number(form.bathrooms),halls:Number(form.halls),kitchens:Number(form.kitchens)};if(editing)await api(`/houses/${editing}`,{method:"PUT",body:JSON.stringify(body)});else await api("/houses",{method:"POST",body:JSON.stringify(body)});setNotice(editing?"House updated successfully.":"House published and is now visible to customers.");setShowAdd(false);setEditing(null);setForm(emptyHouse);load()}catch(e){setNotice(e.message)}}
 async function del(id){if(!confirm("Delete this house?"))return;try{await api(`/houses/${id}`,{method:"DELETE"});setNotice("House deleted.");load()}catch(e){setNotice(e.message)}}
 async function confirmBooking(id){try{await api(`/bookings/${id}/confirm`,{method:"PUT"});setNotice("Booking approved.");load()}catch(e){setNotice(e.message)}}
 async function cancelBooking(id){try{await api(`/bookings/${id}/cancel`,{method:"PUT"});setNotice("Booking cancelled.");load()}catch(e){setNotice(e.message)}}
 function sellerBookingInfo(b){
   if(!b) return {house:null,price:0,paid:0,remaining:0,pending:false};
   const house=houses.find(h=>Number(h.houseId)===Number(b.houseId));
   const price=Number(house?.price||0);
   const paid=payments.filter(p=>Number(p.bookingId)===Number(b.bookingId)&&statusOf(p.status)==="PAID").reduce((a,p)=>a+Number(p.amount||0),0);
   const pending=payments.some(p=>Number(p.bookingId)===Number(b.bookingId)&&statusOf(p.status)==="PENDING");
   return {house,price,paid,remaining:Math.max(0,price-paid),pending};
 }
 async function recordPayment(e){e.preventDefault();try{await api("/payments",{method:"POST",body:JSON.stringify({bookingId:Number(pay.bookingId),amount:Number(pay.amount),paymentDate:new Date().toISOString().slice(0,10),paymentMethod:pay.paymentMethod,status:"PAID"})});setNotice("Payment received. Customer approved, house reserved as SOLD OUT, and receipt/contract are available.");setPay({bookingId:"",amount:"",paymentMethod:"Bank Transfer"});load()}catch(e){setNotice(e.message)}}
 async function confirmPayment(p){try{await api(`/payments/${p.paymentId}/receive`,{method:"PUT"});setNotice(`Payment received from ${p.customerName||"customer"}. Customer approved. Receipt and contract are now available.`);load()}catch(e){setNotice(e.message)}}
 async function completeSale(b){const info=sellerBookingInfo(b);if(info.remaining>0){setNotice(`Remaining balance is ${money(info.remaining)}. Complete sale becomes available after full payment.`);return}try{await api("/sales",{method:"POST",body:JSON.stringify({houseId:b.houseId,customerId:b.customerId,salePrice:info.price,saleDate:new Date().toISOString().slice(0,10)})});setNotice("Sale completed successfully.");load()}catch(e){setNotice(e.message)}}
 function printReport(){const w=window.open("","_blank");if(!w)return;const total=sales.reduce((a,s)=>a+Number(s.salePrice||0),0);w.document.write(`<html><head><title>MAHA E-HOUSING — Seller Sales Report</title><style>@page{size:A4;margin:16mm}body{font-family:Arial,sans-serif;padding:10px;color:#102019}.reportHead{display:flex;align-items:center;gap:18px;border-bottom:3px solid #c7a56a;padding-bottom:18px;margin-bottom:22px}.logo{width:100px;height:100px;object-fit:contain}.brandTitle{font-size:28px;font-weight:800;letter-spacing:.4px}.subtitle{color:#718078;font-size:12px;margin-top:5px}.meta{display:flex;justify-content:space-between;gap:20px;background:#f5f2e9;padding:14px 16px;border-radius:8px;margin-bottom:22px;font-size:12px}table{width:100%;border-collapse:collapse;font-size:11px}th{background:#071711;color:#fff;padding:10px;text-align:left}td{border-bottom:1px solid #e1e5df;padding:10px 8px}tbody tr:nth-child(even){background:#fafbf8}.summary{margin-top:24px;display:flex;justify-content:flex-end}.total{background:#071711;color:#fff;padding:16px 22px;border-radius:9px;min-width:240px}.total small{display:block;color:#b7c2bc;font-size:10px}.total b{display:block;color:#e9d6ac;font-size:20px;margin-top:5px}.foot{margin-top:30px;border-top:1px solid #ddd;padding-top:12px;color:#718078;font-size:10px}</style></head><body><div class="reportHead"><img class="logo" src="${mahaLogo}"/><div><div class="brandTitle">MAHA E-HOUSING</div><div class="subtitle">Official Seller Sales Report</div><div class="subtitle">Your Home, Your Priority</div></div></div><div class="meta"><span><b>Seller:</b> ${auth?.name||""}</span><span><b>Generated:</b> ${new Date().toLocaleDateString("en-TZ")}</span></div><table><thead><tr><th>Sale</th><th>House</th><th>Customer</th><th>Date</th><th>Amount</th><th>Status</th></tr></thead><tbody>${sales.map(s=>`<tr><td>#${s.saleId}</td><td>${s.houseTitle||s.houseId}</td><td>${s.customerName||s.customerId}</td><td>${s.saleDate||""}</td><td>${money(s.salePrice)}</td><td>${s.status||"SOLD"}</td></tr>`).join("")}</tbody></table><div class="summary"><div class="total"><small>${sales.length} COMPLETED SALES</small><b>${money(total)}</b></div></div><div class="foot">MAHA E-HOUSING · Official sales record · Printed from the E-House seller portal</div><script>window.print()</script></body></html>`);w.document.close()}
 return <main className="dashboardShell"><DashboardSidebar role="SELLER" image={auth?.image} name={auth?.name} tab={tab} setTab={setTab} go={go} logout={logout} actionLabel={tab==="houses"?"Add house":"Print sales report"} onAction={tab==="houses"?openAdd:printReport} items={[{id:"overview",label:"Overview",icon:"◈"},{id:"houses",label:"My houses",icon:"⌂"},{id:"bookings",label:"Bookings received",icon:"◷"},{id:"payments",label:"Payments",icon:"◇"},{id:"sales",label:"Completed sales",icon:"✓"}]}/>
 <section className="dashboardMain pagePad"><DashboardTopbar auth={auth} role="Seller"/><div className="dashTop"><div><span className="eyebrow">SELLER PORTAL</span><h1>{tab==="houses"?"My house collection.":tab==="bookings"?"Bookings received.":tab==="payments"?"Payments received.":tab==="sales"?"Completed sales.":`Welcome, ${auth?.name||"seller"}.`}</h1><p>{tab==="overview"?"Manage your property business from one section at a time.":"Only the selected seller workspace is shown."}</p></div>{tab==="houses"&&<button className="primaryBtn" onClick={openAdd}>+ Add house</button>}{tab==="sales"&&<button className="primaryBtn" onClick={printReport}>Print report →</button>}</div>
 <section className="dashContent">
 {tab==="overview"&&<div className="metricRow"><div><small>My houses</small><b>{houses.length}</b></div><div><small>Bookings</small><b>{bookings.length}</b></div><div><small>Payments</small><b>{payments.length}</b></div><div><small>Sales</small><b>{sales.length}</b></div></div>}
 {tab==="houses"&&<div className="dashCard"><div className="sectionHead"><div><span className="eyebrow">MY COLLECTION</span><h2>Houses</h2></div><button className="primaryBtn" onClick={openAdd}>+ Add house</button></div>{houses.length?houses.map((h,i)=><div className="sellerHouse" key={h.houseId}><img src={h.image||images[i%images.length]}/><div><b>{h.title}</b><small>{h.location} · {money(h.price)} · {h.bedrooms} beds · {h.bathrooms} baths · {h.halls??0} halls · {h.kitchens??0} kitchens · <b className={`houseStatus ${statusOf(h.status)==="SOLD_OUT"?"soldOut":"available"}`}>{statusOf(h.status)==="SOLD_OUT"?"SOLD OUT":"AVAILABLE"}</b></small></div><div className="rowActions houseActions"><button className="smallBtn" disabled={statusOf(h.status)==="SOLD_OUT"} title="Edit house" aria-label={`Edit ${h.title||"house"}`} onClick={()=>openEdit(h)}>Edit</button><button className="smallBtn dangerBtn" disabled={statusOf(h.status)==="SOLD_OUT"} title="Delete house" aria-label={`Delete ${h.title||"house"}`} onClick={()=>del(h.houseId)}>Delete</button></div></div>):<p className="muted">No houses yet. Use Add house to publish your first property.</p>}</div>}
 {tab==="bookings"&&<div className="dashCard"><h3>Booking status</h3>{bookings.length?bookings.map(b=>{const st=statusOf(b.status);return <div className="listRow" key={b.bookingId}><span>#{b.bookingId}</span><b>{b.houseTitle||`House #${b.houseId}`}</b><span>{b.customerName||`Customer #${b.customerId}`} · {b.bookingDate}{b.bookingTime?` · ${b.bookingTime}`:""}</span><div className="bookingStatusActions"><em className={`status ${st.toLowerCase()}`}>{st==="CONFIRMED"?"APPROVED":st}</em>{st==="PENDING"&&<><button className="bookingBtn approve" onClick={()=>confirmBooking(b.bookingId)}>✓ Approve</button><button className="bookingBtn reject" onClick={()=>cancelBooking(b.bookingId)}>✕ Reject</button></>}{st==="CONFIRMED"&&sellerBookingInfo(b).remaining<=0&&<span className="paidMark">✓ FULLY PAID · SALE RECORDED</span>}</div></div>;}):<p className="muted">No bookings yet. When a customer requests a viewing, it will appear here.</p>}</div>}
 {tab==="payments"&&<><div className="paymentRoleNotice"><span>✓</span><div><b>Customer payment submission</b><small>Only customers can make and submit payments. As the seller, you only confirm payments that the customer has submitted.</small></div></div><div className="dashCard"><h3>Customer payments & receipts</h3>{payments.length?payments.map(p=><div className="listRow paymentRow" key={p.paymentId}><span>#{p.paymentId}</span><div><b>{p.houseTitle||`Booking #${p.bookingId}`}</b><small className="paymentSub">{p.customerName||`Customer #${p.customerId}`} · Payment: {money(p.amount)} · Paid: {money(p.totalPaid)} · Remaining: {money(p.remainingAmount)}</small></div><span>{p.paymentDate}</span><em className={`status ${String(p.status).toLowerCase()}`}>{statusOf(p.status)==="PENDING"?"PENDING RECEIPT":"RECEIVED"}</em><div className="rowActions">{statusOf(p.status)==="PENDING"&&<button className="smallBtn actionReceipt" onClick={()=>confirmPayment(p)}>Confirm & approve</button>}{statusOf(p.status)==="PAID"&&<><span className="paidMark">✓ RECEIVED</span><button className="smallBtn actionReceipt" onClick={()=>printReceiptReport(p)}>Receipt</button><button className="smallBtn actionReceipt" onClick={()=>printContractReport(p)}>Contract</button></>}</div></div>) : <p className="muted">No payments yet.</p>}</div></>}
 {tab==="sales"&&<DataList title="Completed purchases" data={sales}/>}</section></section>
 {showAdd&&<Modal title={editing?"Edit house":"Add house"} onClose={()=>{setShowAdd(false);setEditing(null)}}><form className="formCard" onSubmit={save}><div className="sellerFormIdentity"><div className="sellerFormAvatar">{auth?.image?<img src={auth.image} alt="Seller"/>:<span>{initials(auth?.name||"Seller")}</span>}</div><div><span className="eyebrow">SELLER</span><b>{auth?.name||"Current seller"}</b><small>{auth?.email||"Authenticated seller account"}</small></div></div><input required placeholder="House title" value={form.title} onChange={e=>setForm({...form,title:e.target.value})}/><input required placeholder="Location" value={form.location} onChange={e=>setForm({...form,location:e.target.value})}/><textarea required placeholder="Description" value={form.description} onChange={e=>setForm({...form,description:e.target.value})}/><input type="number" min="1" required placeholder="Price (TZS)" value={form.price} onChange={e=>setForm({...form,price:e.target.value})}/><div className="two"><input type="number" min="1" required placeholder="Bedrooms" value={form.bedrooms} onChange={e=>setForm({...form,bedrooms:e.target.value})}/><input type="number" min="1" required placeholder="Bathrooms" value={form.bathrooms} onChange={e=>setForm({...form,bathrooms:e.target.value})}/></div><div className="two"><input type="number" min="0" required placeholder="Halls" value={form.halls} onChange={e=>setForm({...form,halls:e.target.value})}/><input type="number" min="0" required placeholder="Kitchens" value={form.kitchens} onChange={e=>setForm({...form,kitchens:e.target.value})}/></div><div className="spaceTotal">TOTAL SPACES <strong>{Number(form.bedrooms||0)+Number(form.bathrooms||0)+Number(form.halls||0)+Number(form.kitchens||0)}</strong></div><label className="fileField">House picture<input type="file" accept="image/*" onChange={e=>readImage(e.target.files?.[0],image=>setForm({...form,image}))}/></label><button className="primaryBtn wide">{editing?"Update house":"Publish house"} →</button></form></Modal>}
 </main>
}

function Modal({title,onClose,children}){return <div className="modalBackdrop" onMouseDown={e=>{if(e.target===e.currentTarget)onClose()}}><div className="modalBox"><div className="modalHead"><div><span className="eyebrow">MAHA E-HOUSING</span><h2>{title}</h2></div><button type="button" className="modalClose" onClick={onClose}>×</button></div>{children}</div></div>}

function AdminPeopleCollection({title,type,data,onEdit,onDelete,onSelect,onAdd}){
 const [query,setQuery]=useState("");
 const isSeller=type==="sellers";
 const filtered=data.filter(row=>`${row.name||""} ${row.email||""} ${row.nida||""} ${row[isSeller?"sellerId":"customerId"]||""}`.toLowerCase().includes(query.toLowerCase()));
 return <div className="peopleCollection"><div className="collectionHead"><div><span className="eyebrow">DIRECTORY</span><h2>{title}</h2><p className="muted">{data.length} registered {type}.</p></div><div className="collectionActions">{onAdd&&<button className="primaryBtn" onClick={onAdd}>+ Add seller</button>}<input className="collectionSearch" value={query} onChange={e=>setQuery(e.target.value)} placeholder={`Search ${type}...`}/></div></div>{filtered.length?<div className="peopleGrid">{filtered.map((row,i)=>{const id=isSeller?row.sellerId:row.customerId;return <article className={`personCard selectable ${onSelect?"clickable":""}`} key={id||i} onClick={()=>onSelect&&onSelect(row)}>{row.image?<img className="personPhoto" src={row.image} alt=""/>:<div className="personPhoto placeholder">{initials(row.name||"User")}</div>}<div className="personInfo"><div className="personTop"><span className="personRole">{isSeller?"SELLER":"CUSTOMER"}</span><b>#{id??"—"}</b></div><h3>{row.name||"Unnamed user"}</h3><div className="personDetails"><span><strong>Email</strong>{row.email||"—"}</span><span><strong>NIDA</strong>{row.nida||"—"}</span></div>{onEdit&&<div className="personActions"><button className="smallBtn" title={`Edit ${isSeller?"seller":"customer"}`} aria-label={`Edit ${row.name||type.slice(0,-1)}`} onClick={e=>{e.stopPropagation();onEdit(row)}}>Edit</button><button className="smallBtn dangerBtn" title={`Delete ${isSeller?"seller":"customer"}`} aria-label={`Delete ${row.name||type.slice(0,-1)}`} onClick={e=>{e.stopPropagation();onDelete(row)}}>Delete</button></div>}</div></article>})}</div>:<div className="empty"><h3>No {type} found</h3><p>Try another search.</p></div>}</div>
}

function AdminDesk({auth,setNotice,go,logout}){
 const [tab,setTab]=useState("overview"),[records,setRecords]=useState({sellers:[],customers:[],houses:[],bookings:[],payments:[],sales:[],documents:[]});
 const emptySeller={name:"",email:"",phone:"",address:"",nida:"",password:"",image:""};
 const emptyCustomer={name:"",email:"",phone:"",address:"",nida:"",password:"",image:""};
 const [sellerForm,setSellerForm]=useState(emptySeller),[busy,setBusy]=useState(false),[showSeller,setShowSeller]=useState(false),[editingSeller,setEditingSeller]=useState(null),[selectedSeller,setSelectedSeller]=useState(null);
 const [customerForm,setCustomerForm]=useState(emptyCustomer),[showCustomer,setShowCustomer]=useState(false),[editingCustomer,setEditingCustomer]=useState(null);
 async function loadAll(){try{const names=Object.keys(records);const values=await Promise.all(names.map(n=>api(`/management/${n}`)));const next={};names.forEach((n,i)=>next[n]=Array.isArray(values[i])?values[i]:[]);setRecords(next)}catch(e){setNotice(e.message)}}
 useEffect(()=>{loadAll()},[]);
 function openAddSeller(){setEditingSeller(null);setSellerForm(emptySeller);setShowSeller(true)}
 function openEditSeller(row){setEditingSeller(row.sellerId);setSellerForm({...row,password:""});setShowSeller(true)}
 async function saveSeller(e){e.preventDefault();setBusy(true);try{const body={...sellerForm};if(editingSeller){if(!body.password)body.password="ChangeMe123!";await api(`/management/sellers/${editingSeller}`,{method:"PUT",body:JSON.stringify(body)});setNotice("Seller updated successfully.")}else{await api("/management/sellers",{method:"POST",body:JSON.stringify(body)});setNotice("Seller account created successfully.")}setShowSeller(false);setEditingSeller(null);setSellerForm(emptySeller);await loadAll()}catch(e){setNotice(e.message)}finally{setBusy(false)}}
 async function deleteSeller(row){if(!confirm(`Delete seller ${row.name}? This is allowed only when the seller has no houses.`))return;try{await api(`/management/sellers/${row.sellerId}`,{method:"DELETE"});setNotice("Seller deleted.");setSelectedSeller(null);loadAll()}catch(e){setNotice(e.message)}}
 function openEditCustomer(row){setEditingCustomer(row.customerId);setCustomerForm({...row,password:""});setShowCustomer(true)}
 async function saveCustomer(e){e.preventDefault();setBusy(true);try{const body={...customerForm};await api(`/management/customers/${editingCustomer}`,{method:"PUT",body:JSON.stringify(body)});setNotice("Customer updated successfully.");setShowCustomer(false);setEditingCustomer(null);setCustomerForm(emptyCustomer);await loadAll()}catch(e){setNotice(e.message)}finally{setBusy(false)}}
 async function deleteCustomer(row){if(!confirm(`Delete customer ${row.name}? This is allowed only when the customer has no bookings or sales.`))return;try{await api(`/management/customers/${row.customerId}`,{method:"DELETE"});setNotice("Customer deleted.");await loadAll()}catch(e){setNotice(e.message)}}
 const totalSales=records.sales.reduce((a,s)=>a+Number(s.salePrice||0),0),paid=records.payments.reduce((a,p)=>a+Number(p.amount||0),0); const sellerMap={};records.houses.forEach(h=>sellerMap[h.houseId]=h.sellerName||`Seller ${h.sellerId||""}`);const grouped={};records.sales.forEach(s=>{const seller=sellerMap[s.houseId]||"Unknown seller";grouped[seller]=(grouped[seller]||0)+Number(s.salePrice||0)});
 function printSellerSales(){const w=window.open("","_blank");if(!w)return;w.document.write(`<html><head><title>MAHA E-HOUSING — Sales by Seller</title><style>@page{size:A4;margin:16mm}body{font-family:Arial,sans-serif;padding:10px;color:#102019}.reportHead{display:flex;align-items:center;gap:18px;border-bottom:3px solid #c7a56a;padding-bottom:18px;margin-bottom:22px}.logo{width:100px;height:100px;object-fit:contain}.brandTitle{font-size:28px;font-weight:800}.subtitle{color:#718078;font-size:12px;margin-top:5px}table{width:100%;border-collapse:collapse;font-size:12px}th{background:#071711;color:#fff;padding:11px;text-align:left}td{border-bottom:1px solid #e1e5df;padding:11px 8px}.summary{margin-top:24px;display:flex;justify-content:flex-end}.total{background:#071711;color:#fff;padding:16px 22px;border-radius:9px;min-width:240px}.total small{display:block;color:#b7c2bc;font-size:10px}.total b{display:block;color:#e9d6ac;font-size:20px;margin-top:5px}.foot{margin-top:30px;border-top:1px solid #ddd;padding-top:12px;color:#718078;font-size:10px}</style></head><body><div class="reportHead"><img class="logo" src="${mahaLogo}"/><div><div class="brandTitle">MAHA E-HOUSING</div><div class="subtitle">Official Sales by Seller Report</div><div class="subtitle">Your Home, Your Priority</div></div></div><p><b>Generated:</b> ${new Date().toLocaleDateString("en-TZ")}</p><table><thead><tr><th>Seller</th><th>Sales value</th></tr></thead><tbody>${Object.entries(grouped).map(([n,v])=>`<tr><td>${n}</td><td>${money(v)}</td></tr>`).join("")}</tbody></table><div class="summary"><div class="total"><small>TOTAL SALES VALUE</small><b>${money(totalSales)}</b></div></div><div class="foot">MAHA E-HOUSING · Official management sales record · Printed from the E-House admin portal</div><script>window.print()</script></body></html>`);w.document.close()}
 const title={overview:"Control center",sellers:"Seller collection",customers:"Customer collection",houses:"All houses",bookings:"Bookings",payments:"Payments",sales:"Sales by seller"}[tab];
 return <main className="dashboardShell"><DashboardSidebar role="ADMIN" image={auth?.image} name={auth?.name||"Administrator"} tab={tab} setTab={setTab} go={go} logout={logout} actionLabel={tab==="sellers"?"Add seller":tab==="sales"?"Print seller report":null} onAction={tab==="sellers"?openAddSeller:printSellerSales} items={[{id:"overview",label:"Overview",icon:"◈"},{id:"sellers",label:"Sellers",icon:"♙"},{id:"customers",label:"Customers",icon:"♧"},{id:"houses",label:"All houses",icon:"⌂"},{id:"bookings",label:"Bookings",icon:"◷"},{id:"payments",label:"Payments",icon:"◇"},{id:"sales",label:"Sales by seller",icon:"✓"}]}/><section className="dashboardMain pagePad"><DashboardTopbar auth={auth} role="Admin"/><div className="dashTop"><div><span className="eyebrow">ADMIN PORTAL</span><h1>MAHA E-HOUSING<br/>{title}.</h1><p>{tab==="sellers"?"Manage seller accounts and open an individual seller record.":tab==="customers"?"Manage customer profiles, update their details, or remove eligible records.":"Use the sidebar to open one management area at a time."}</p></div>{tab==="sellers"&&<button className="primaryBtn" onClick={openAddSeller}>+ Add seller</button>}{tab==="sales"&&<button className="primaryBtn" onClick={printSellerSales}>Print report →</button>}</div>
 {tab==="overview"&&<div className="dashContent"><div className="adminOverviewGrid"><div className="metricRow adminCounts"><div><small>Total customers</small><b>{records.customers.length}</b><span>Registered customers</span></div><div><small>Total sellers</small><b>{records.sellers.length}</b><span>Active sellers</span></div><div><small>Total houses</small><b>{records.houses.length}</b><span>Property listings</span></div><div><small>Total sales</small><b>{records.sales.length}</b><span>Completed sales</span></div></div><div className="adminFinanceGrid"><div className="financeCard received"><div><small>Total received payments</small><span className="financeLabel">ALL PAID TRANSACTIONS</span></div><b>{money(paid)}</b><p>Money successfully received through E-House</p></div><div className="financeCard salesValue"><div><small>Total sales value</small><span className="financeLabel">COMPLETED SALES</span></div><b>{money(totalSales)}</b><p>Value of completed property sales</p></div></div></div><div className="dashCard"><h3>Management center</h3><p className="muted">Select one collection from the sidebar. Seller and customer records are kept separate.</p></div></div>}
 {tab==="sellers"&&<><AdminPeopleCollection title="All sellers" type="sellers" data={records.sellers} onAdd={openAddSeller} onEdit={openEditSeller} onDelete={deleteSeller} onSelect={setSelectedSeller}/>{selectedSeller&&<div className="dashCard selectedPerson"><div className="sectionHead"><div><span className="eyebrow">SELLER DETAILS</span><h2>{selectedSeller.name}</h2></div><div className="rowActions detailActions"><button className="primaryBtn" onClick={openAddSeller}>+ Add seller</button><button type="button" className="outlineBtn" onClick={()=>openEditSeller(selectedSeller)}>Edit seller</button></div></div><div className="sellerDetailGrid"><img className="personPhoto" src={selectedSeller.image||""} alt=""/><div><b>Seller ID: #{selectedSeller.sellerId}</b><p>Email: {selectedSeller.email}</p><p>NIDA: {selectedSeller.nida}</p><p>Phone: {selectedSeller.phone}</p><p>Address: {selectedSeller.address}</p></div></div></div>}</>}
 {tab==="customers"&&<AdminPeopleCollection title="All customers" type="customers" data={records.customers} onEdit={openEditCustomer} onDelete={deleteCustomer}/>} 
 {tab==="houses"&&<div className="tableCard"><AdminTable data={records.houses} empty="No houses yet"/></div>}
 {tab==="bookings"&&<div className="tableCard"><AdminTable data={records.bookings} empty="No bookings yet"/></div>}
 {tab==="payments"&&<div className="tableCard"><AdminTable data={records.payments} empty="No payments yet"/></div>}
 {tab==="sales"&&<div className="dashCard"><div className="sectionHead"><div><span className="eyebrow">REPORT</span><h2>Sales by seller</h2></div><button className="primaryBtn" onClick={printSellerSales}>Print report →</button></div>{Object.keys(grouped).length?Object.entries(grouped).map(([name,value])=><div className="listRow" key={name}><b>{name}</b><span>{money(value)}</span><em>SALES VALUE</em></div>):<p className="muted">No completed sales yet.</p>}</div>}
 </section>{showSeller&&<Modal title={editingSeller?"Edit seller":"Add seller"} onClose={()=>{setShowSeller(false);setEditingSeller(null)}}><form className="formCard" onSubmit={saveSeller}><input placeholder="Seller full name" required value={sellerForm.name} onChange={e=>setSellerForm({...sellerForm,name:e.target.value})}/><input type="email" placeholder="Seller email" required value={sellerForm.email} onChange={e=>setSellerForm({...sellerForm,email:e.target.value})}/><div className="two"><input placeholder="Phone" required value={sellerForm.phone} onChange={e=>setSellerForm({...sellerForm,phone:e.target.value})}/><input placeholder="NIDA" required value={sellerForm.nida} onChange={e=>setSellerForm({...sellerForm,nida:e.target.value})}/></div><input placeholder="Address" required value={sellerForm.address} onChange={e=>setSellerForm({...sellerForm,address:e.target.value})}/><label className="fileField">Seller passport picture<input type="file" accept="image/*" onChange={e=>readImage(e.target.files?.[0],image=>setSellerForm({...sellerForm,image}))}/></label><input type="password" minLength="6" placeholder={editingSeller?"New password (optional)":"Seller password"} required={!editingSeller} value={sellerForm.password} onChange={e=>setSellerForm({...sellerForm,password:e.target.value})}/><button className="primaryBtn wide" disabled={busy}>{busy?"Saving…":editingSeller?"Update seller →":"Create seller →"}</button></form></Modal>}{showCustomer&&<Modal title="Edit customer" onClose={()=>{setShowCustomer(false);setEditingCustomer(null)}}><form className="formCard" onSubmit={saveCustomer}><input placeholder="Customer full name" required value={customerForm.name} onChange={e=>setCustomerForm({...customerForm,name:e.target.value})}/><input type="email" placeholder="Customer email" required value={customerForm.email} onChange={e=>setCustomerForm({...customerForm,email:e.target.value})}/><div className="two"><input placeholder="Phone" required value={customerForm.phone} onChange={e=>setCustomerForm({...customerForm,phone:e.target.value})}/><input placeholder="NIDA" required value={customerForm.nida} onChange={e=>setCustomerForm({...customerForm,nida:e.target.value})}/></div><input placeholder="Address" required value={customerForm.address} onChange={e=>setCustomerForm({...customerForm,address:e.target.value})}/><label className="fileField">Customer passport picture<input type="file" accept="image/*" onChange={e=>readImage(e.target.files?.[0],image=>setCustomerForm({...customerForm,image}))}/></label><input type="password" minLength="6" placeholder="New password (optional)" value={customerForm.password} onChange={e=>setCustomerForm({...customerForm,password:e.target.value})}/><button className="primaryBtn wide" disabled={busy}>{busy?"Saving…":"Update customer →"}</button></form></Modal>}</main>
}

function AdminTable({data,empty}){return data.length?<table><thead><tr>{Object.keys(data[0]).filter(k=>k!=="image").slice(0,7).map(k=><th key={k}>{k}</th>)}</tr></thead><tbody>{data.map((row,i)=><tr key={i}>{Object.entries(row).filter(([k])=>k!=="image").slice(0,7).map(([k,v],j)=><td key={j}>{typeof v==="object"?JSON.stringify(v):String(v??"")}</td>)}</tr>)}</tbody></table>:<div className="empty"><h3>{empty}</h3></div>}

createRoot(document.getElementById("root")).render(<App/>);
