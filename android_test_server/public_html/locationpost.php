<?php
include("config.php");
if($_SERVER["REQUEST_METHOD"] == "POST")
{
// username and password sent from Form
$myusername=mysqli_real_escape_string($db,$_POST['username']);
$mylatitude=mysqli_real_escape_string($db,$_POST['latitude']);
$mylongitude=mysqli_real_escape_string($db,$_POST['longitude']);
$myrstatus=mysqli_real_escape_string($db,$_POST['rstatus']);
$myconnecter=mysqli_real_escape_string($db,$_POST['connecter']);

$sql="SELECT * FROM admin WHERE username='$myusername';";
$result=mysqli_query($db,$sql);


if ($result->num_rows > 0) {
	$row = $result->fetch_assoc();
	//echo "id: " . $row["id"]. " - Name: " . $row["username"]. " " . $row["passcode"]. "<br><br><br><br><br>";
	$id=$row["id"];
	if(empty($_POST['rstatus']) || empty($_POST['connecter']))
		{$sql2="SELECT * FROM location WHERE id='$id';";
		 $result2=mysqli_query($db,$sql2);
		 $row2 = $result2->fetch_assoc();
		 $myrstatus= $row2["rstatus"];
		 $myconnecter= $row2["connecter"];
		}
	$sql1="UPDATE location SET latitude='$mylatitude', longitude='$mylongitude', rstatus='$myrstatus', connecter='$myconnecter' WHERE id='$id';";
	$result1=mysqli_query($db,$sql1);
	//$data = '{"latitude":'$mylatitude', "longitude": '$mylongitude', "rstatus": '$myrstatus',"connecter":'$myconnecter'}';
	$data = array('username'=>$myusername, 'latitude'=>$mylatitude, 'longitude'=>$mylongitude,'rstatus'=>$myrstatus,'connecter'=>$myconnecter);
	header('Content-Type: application/json');
	echo json_encode($data);
}
else { echo "0 results";}
}
?>

