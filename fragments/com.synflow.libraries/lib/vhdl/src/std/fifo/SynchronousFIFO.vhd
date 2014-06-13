-------------------------------------------------------------------------------
-- Copyright (c) 2012-2013 Synflow SAS.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--    Nicolas Siret - initial API and implementation and/or initial documentation
--    Matthieu Wipliez - refactoring and maintenance
-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
-- Title      : Synchronous FIFO
-- Author     : Nicolas Siret (nicolas.siret@synflow.com)
--              Matthieu Wipliez (matthieu.wipliez@synflow.com)
-- Standard   : VHDL'93
-------------------------------------------------------------------------------


library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;
use ieee.numeric_std.all;

-------------------------------------------------------------------------------

entity SynchronousFIFO is
  generic (
    depth : integer := 8;
    width : integer := 16);
  port (
    reset_n          : in  std_logic;
    clock            : in  std_logic;
                                        -- write data
    din_send         : in  std_logic;
    din              : in  std_logic_vector(width - 1 downto 0);
                                        -- read data
    dout_send        : out std_logic;
    dout             : out std_logic_vector(width - 1 downto 0);
                                        -- Ready to send
    ready            : in  std_logic;
                                        -- Flags
    full             : out std_logic;
    almost_full      : out std_logic;
    empty            : out std_logic
    );
end SynchronousFIFO;

-------------------------------------------------------------------------------

architecture arch_Synchronous_fifo of SynchronousFIFO is

  -----------------------------------------------------------------------------
  -- Signals declaration
  -----------------------------------------------------------------------------
  signal full_i          : std_logic;
  signal empty_i         : std_logic;
  --
  signal wr_enable       : std_logic;
  signal rd_enable       : std_logic;
  --
  signal rd_address      : unsigned(depth - 1 downto 0);
  signal wr_address      : unsigned(depth - 1 downto 0);
  -------------------------------------------------------------------------------

begin

  -- full and empty flag
  full             <= full_i;
  empty            <= empty_i;

  process (reset_n, clock) is
  begin
    if reset_n = '0' then
      dout_send <= '0';
    elsif rising_edge(clock) then
      dout_send <= rd_enable;
    end if;
  end process;

  -- wr_enable and rd_enable are active iff the flags allow it
  wr_enable <= din_send and not full_i;
  rd_enable <= ready and not empty_i;

  ram : entity work.Dual_Port_RAM
    generic map (
      depth => depth,
      width => width)
    port map (
      wr_clock        => clock,
      rd_clock        => clock,
      reset_n         => reset_n,
      wr_address      => std_logic_vector(wr_address),
      data            => din,
      data_send       => wr_enable,
      rd_address      => std_logic_vector(rd_address),
      q               => dout);

  wr_ctrl : entity work.FIFO_Write_Controller
    generic map (
      depth => depth)
    port map (
      reset_n     => reset_n,
      wr_clock    => clock,
      enable      => wr_enable,
      rd_address  => rd_address,
      full        => full_i,
      wr_address  => wr_address,
      almost_full => almost_full);

  rd_ctrl : entity work.FIFO_Read_Controller
    generic map (
      depth => depth)
    port map (
      reset_n    => reset_n,
      rd_clock   => clock,
      enable     => rd_enable,
      wr_address => wr_address,
      empty      => empty_i,
      rd_address => rd_address);

end arch_Synchronous_fifo;
