
/* AirSimulation class
 *
 * TP of SE (version 2020)
 *
 * AM
 */

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.ArrayList;

public class AirSimulation
{




	private int nAgent1;
	private int nAgent2;
	private int nAgent3;
	private int nAgent4;
	private Aircraft a;
	public final int nagents = 4;

	// Constructor
	public AirSimulation()
	{
		this.nAgent1 = 0;
		this.nAgent2 = 0;
		this.nAgent3 = 0;
		this.nAgent4 = 0;
		this.a = new Aircraft();  // standard model
	}

	// Reference to Aircraft
	public Aircraft getAircraftRef()
	{
		return this.a;
	}



	// Agent1
	public void agent1() throws InterruptedException
	{
		boolean placed = false;
		Random R = new Random();
		ArrayList<Integer> emergRows = this.a.getEmergencyRowList();

		// generating a new Customer
		Customer c = new Customer();

		// randomly pick a seat
		do
		{
			int row = R.nextInt(this.a.getNumberOfRows());
			int col = R.nextInt(this.a.getSeatsPerRow());

			// verifying whether the seat is free
			if (this.a.isSeatEmpty(row,col))
			{
				// if this is an emergency exit seat, and c is over60, then we skip
				if (!emergRows.contains(row) || !c.isOver60() || this.a.numberOfFreeSeats() <= this.a.getSeatsPerRow() * this.a.getNumberEmergencyRows())
				{
					this.a.add(c,row,col);
					placed = true;
				}
			}
		}
		while (!placed && !this.a.isFlightFull());

		// updating counter
		if (placed)  this.nAgent1++;
	}

	// Agent2
	public void agent2() throws InterruptedException
	{
		boolean placed = false;
		ArrayList<Integer> emergRows = this.a.getEmergencyRowList();

		// generating a new Customer
		Customer c = new Customer();

		// searching free seats on the seatMap
		int row = 0;
		while (!placed && !this.a.isFlightFull() && row < this.a.getNumberOfRows())
		{
			int col = 0;
			while (!placed && col < this.a.getSeatsPerRow())
			{
				// verifying whether the seat is free
				if (this.a.isSeatEmpty(row,col))
				{
					// if this is an emergency exit seat, and c needs assistence, then we skip
					if (!emergRows.contains(row) || !c.needsAssistence() || this.a.numberOfFreeSeats() <= this.a.getSeatsPerRow() * this.a.getNumberEmergencyRows())
					{
						this.a.add(c,row,col);
						placed = true;
					}
				}
				col++;
			}
			row++;
		}

		// updating counter
		if (placed)  this.nAgent2++;
	}

	// Agent3

	//FIXME


	int numTentative=0;
	public void agent3() throws InterruptedException
	{
		Random R = new Random();
		Customer tmp = new Customer();
		Customer tmp2 = new Customer();

		boolean exchange = false;


		// randomly pick two seat
		do
		{

			int row1 = R.nextInt(this.a.getNumberOfRows());
			int col1 = R.nextInt(this.a.getSeatsPerRow());

			int row2 = R.nextInt(this.a.getNumberOfRows());
			int col2 = R.nextInt(this.a.getSeatsPerRow());

			// verifying whether the seat is use by two customers
			if (!this.a.isSeatEmpty(row1,col1) && !this.a.isSeatEmpty(row2,col2))
			{
				// A customer with a most frequent flyer is not on a place more in the front of aircraft that the other customer , we exhange they place.
				if (   ((this.a.getCustomer(row1,col1).getFlyerLevel()) < (this.a.getCustomer(row2,col2).getFlyerLevel()) && (row1 < row2)) ||  ((this.a.getCustomer(row1,col1).getFlyerLevel()) > (this.a.getCustomer(row2,col2).getFlyerLevel()) && (row1 > row2)) )
				{
					//compteur
					numTentative++;
					//System.out.println("Agent 3 tantative N�" + numTentative);

					//clone customer
					tmp=(Customer) this.a.getCustomer(row1,col1).clone();
					tmp2=(Customer) this.a.getCustomer(row2,col2).clone();
					//free seat of two customer
					this.a.freeSeat(row1,col1);
					this.a.freeSeat(row2,col2);
					//exchange the customer
					this.a.add(tmp2, row1, col1);
					this.a.add(tmp, row2, col2);


					exchange = true;
				}
			}
		}
		while (!exchange);


		if(exchange) {this.nAgent3++;}
	}

	// Agent4: the virus
	public void agent4() throws InterruptedException
	{

		for (int i = 0; i < this.a.getNumberOfRows(); i++)
		{
			for (int j = 0; j < this.a.getSeatsPerRow(); j++)
			{
				Customer c = this.a.getCustomer(i,j);
				this.a.freeSeat(i,j);
				if (c != null) this.a.add(c,i,j);
			}
		}
		this.nAgent4++;

		// to be completed ...

	}

	// Resetting
	public void reset()
	{
		this.nAgent1 = 0;
		this.nAgent2 = 0;
		this.nAgent3 = 0;
		this.nAgent4 = 0;
		this.a.reset();
	}

	// Printing
	public String toString()
	{
		String print = "AirSimulation (agent1 : " + this.nAgent1 + ", agent2 : " + this.nAgent2 + ", " +
				"agent3 : " + this.nAgent3 + ", agent4 : " + this.nAgent4 + ")\n";
		print = print + a.toString();
		return print;
	}





	
	
	
	// temps sans threads : ~33ms

	// temps avec 2 threads (agent1 & agent2,3,4) avec sémpahores : ~40ms

	//temps avec 2 threads (agent1 & agent2,3,4) sans sémpahores : ~40ms

	// Simulation in sequential (main)
	public static void main(String[] args) throws InterruptedException
	{
		Semaphore sem = new Semaphore(4);
		long chono = System.currentTimeMillis();
		System.out.println("\n** Sequential execution **\n");
		if (args != null && args.length > 0 && args[0] != null && args[0].equals("animation"))
		{
			AirSimulation s = new AirSimulation();
			while (!s.a.isFlightFull())
			{
				Thread troisAgent = new Thread() {
					public void run() {
						try {
							sem.acquire();
							synchronized (s) {
								s.agent2();
								s.agent3();
								s.agent4();
							}
							sem.release();
						}
						catch(Exception e) {
							System.out.println("An error as occured");
						}
					}
				};
				
				
				Thread agent1T = new Thread() {
					public void run() {
						try {
							sem.acquire();
							synchronized (s) {
								s.agent1();	
							}
							troisAgent.start();
							sem.release();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

				};
				agent1T.start();

				System.out.println(s + s.a.cleanString());
				//Thread.sleep(100);
			}
			System.out.println(s);
		}
		else
		{
			AirSimulation s = new AirSimulation();
			while (!s.a.isFlightFull())
			{
				Thread troisAgent = new Thread() {
					public void run() {
						try {
							sem.acquire();
							synchronized (s) {
								s.agent2();
								s.agent3();
								s.agent4();
							}
							sem.release();
						}
						catch(Exception e) {
							System.out.println("An error as occured");
						}
					}
				};
				
				
				Thread agent1T = new Thread() {
					public void run() {
						try {
							sem.acquire();
							synchronized (s) {
								s.agent1();	
							}
							troisAgent.start();
							sem.release();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

				};
				agent1T.start();
			}
			synchronized (s) {
				System.out.println(s);	
			}
		}
		System.out.println(System.currentTimeMillis() - chono+"ms");
	}
}
